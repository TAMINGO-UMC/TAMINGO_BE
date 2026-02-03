package app.tamingo.domain.monthlyreport.batch;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.gpt.prompt.common.DataPrompt;
import app.tamingo.domain.gpt.prompt.monthlyreport.MonthlyInsightPrompt;
import app.tamingo.domain.gpt.service.monthlyreport.MonthlyInsightGptService;
import app.tamingo.domain.monthlyreport.dto.MonthlyInsightGptResponse;
import app.tamingo.domain.monthlyreport.entity.MonthlyInsight;
import app.tamingo.domain.monthlyreport.entity.MonthlyReport;
import app.tamingo.domain.monthlyreport.entity.MonthlyWeekSummary;
import app.tamingo.domain.monthlyreport.enums.MonthlyInsightType;
import app.tamingo.domain.monthlyreport.exception.MonthlyReportErrorCode;
import app.tamingo.domain.monthlyreport.repository.MonthlyReportRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleResult;
import app.tamingo.domain.schedule.enums.ScheduleResultStatus;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.schedule.repository.ScheduleResultRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.weeklyreport.enums.ProductivityGrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyReportBatchWorker {

    private final UserRepository userRepository;
    private final MonthlyReportRepository monthlyReportRepository;
    private final MonthlyInsightGptService monthlyInsightGptService;
    private final MonthlyInsightPrompt monthlyInsightPrompt;
    private final TodoRepository todoRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleResultRepository scheduleResultRepository;

    /**
     * 유저 1명 월간 리포트 생성은 유저 단위 트랜잭션으로 처리
     * monthStartDate(해당 월 1일) ~ monthEndDate(말일) 범위의
     * 월간 리포트를 유저에 대해 생성/갱신한다.
     *
     * - 멱등성 보장: (user_id, month_start_date) 유니크 기반 upsert
     * - weekSummaries는 orphanRemoval로 "교체" 방식
     * - insights도 GPT 성공 시에만 교체
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateOneUser(Long userId, YearMonth ym, LocalDate monthStart, LocalDate monthEnd) {

        // -------------------------
        // 1) 원천 데이터 조회
        // -------------------------
        List<Todo> todos = todoRepository.findAllByUserIdAndTargetDateBetween(userId, monthStart, monthEnd);


        LocalDateTime scheduleStart = monthStart.atStartOfDay();
        LocalDateTime scheduleEndExclusive = monthEnd.plusDays(1).atStartOfDay();

        List<Schedule> schedules =
                scheduleRepository.findAllByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        userId, scheduleStart, scheduleEndExclusive
                );

        List<Long> scheduleIds = schedules.stream().map(Schedule::getId).toList();

        Map<Long, ScheduleResult> resultByScheduleId = scheduleIds.isEmpty()
                ? Map.of()
                : scheduleResultRepository.findAllByScheduleIdIn(scheduleIds).stream()
                .collect(Collectors.toMap(
                        ScheduleResult::getScheduleId,
                        Function.identity(),
                        (a, b) -> a // 중복이면 첫 번째 유지
                ));

        // 확정 결과만 집계 (PENDING/CANCELED 제외)
        List<ScheduleResult> finalizedResults = schedules.stream()
                .map(s -> resultByScheduleId.get(s.getId()))
                .filter(Objects::nonNull)
                .filter(r -> r.getStatus() != ScheduleResultStatus.PENDING)
                .filter(r -> r.getStatus() != ScheduleResultStatus.CANCELED)
                .toList();

        // -------------------------
        // 2) Todo 지표
        // -------------------------
        int taskTotal = todos.size();
        int taskDone = (int) todos.stream().filter(Todo::isChecked).count();
        BigDecimal taskCompletionRate = percent(taskDone, taskTotal);
        BigDecimal taskScore = taskCompletionRate; // 정책상 동일(확장 대비 분리)

        // -------------------------
        // 3) Schedule 지표
        // -------------------------
        int scheduleTotal = finalizedResults.size();
        int onTimeCount = (int) finalizedResults.stream()
                .filter(r -> r.getStatus() == ScheduleResultStatus.ON_TIME)
                .count();

        BigDecimal onTimeRate = percent(onTimeCount, scheduleTotal);
        BigDecimal onTimeScore = avgPunctualityScore(finalizedResults);
        Integer avgLateMinutes = avgLateMinutes(finalizedResults);

        // -------------------------
        // 4) Navigation Bonus
        // 길찾기 시작 후 도착(ON_TIME/LATE)한 횟수당 +1
        // -------------------------
        int navigationBonus = (int) finalizedResults.stream()
                .filter(r -> Boolean.TRUE.equals(r.getNavigationUsed()))
                .filter(r -> r.getStatus() == ScheduleResultStatus.ON_TIME || r.getStatus() == ScheduleResultStatus.LATE)
                .count();

        // -------------------------
        // 5) Active Days (월간: 0~말일까지)
        // -------------------------
        int activeDays = calcActiveDays(todos, schedules, monthStart, monthEnd);

        // -------------------------
        // 6) Productivity Score + Grade
        // (주간 수식 동일, D/7 -> D/daysInMonth)
        // -------------------------
        int daysInMonth = ym.lengthOfMonth();
        int productivityScore = calcProductivityScore(onTimeScore, taskScore, activeDays, navigationBonus, daysInMonth);
        ProductivityGrade grade = toGrade(productivityScore);

        // -------------------------
        // 7) Diff (지난달 대비)
        // -------------------------
        YearMonth prevYm = ym.minusMonths(1);
        LocalDate prevMonthStart = prevYm.atDay(1);

        BigDecimal onTimeDiff = null;
        BigDecimal taskCompletionDiff = null;

        Optional<MonthlyReport> prevOpt =
                monthlyReportRepository.findByUserIdAndMonthStartDate(userId, prevMonthStart);

        if (prevOpt.isPresent()) {
            MonthlyReport prev = prevOpt.get();
            onTimeDiff = onTimeRate.subtract(prev.getOnTimeRate()).setScale(2, RoundingMode.HALF_UP);
            taskCompletionDiff = taskCompletionRate.subtract(prev.getTaskCompletionRate()).setScale(2, RoundingMode.HALF_UP);
        }

        // -------------------------
        // 8) MonthlyReport upsert
        // -------------------------
        Optional<MonthlyReport> reportOpt =
                monthlyReportRepository.findByUserIdAndMonthStartDate(userId, monthStart);

        MonthlyReport report;
        if (reportOpt.isPresent()) {
            report = reportOpt.get();
            report.updateMetrics(
                    monthEnd,
                    onTimeRate, onTimeScore, onTimeDiff,
                    taskCompletionRate, taskScore, taskCompletionDiff,
                    taskDone, taskTotal,
                    productivityScore, grade,
                    activeDays, navigationBonus,
                    avgLateMinutes
            );
        } else {
            User userRef = userRepository.getReferenceById(userId);
            report = MonthlyReport.of(
                    userRef,
                    monthStart,
                    monthEnd,
                    onTimeRate, onTimeScore, onTimeDiff,
                    taskCompletionRate, taskScore, taskCompletionDiff,
                    taskDone, taskTotal,
                    productivityScore, grade,
                    activeDays, navigationBonus,
                    avgLateMinutes
            );
        }

        // -------------------------
        // 9) MonthlyWeekSummary(주차별 1~4) 교체 생성
        // -------------------------
        List<MonthlyWeekSummary> newWeekSummaries = buildWeekSummaries(
                report, ym, todos, schedules, resultByScheduleId
        );

        report.getWeekSummaries().clear();
        report.getWeekSummaries().addAll(newWeekSummaries);

        // -------------------------
        // 10) GPT Monthly Insights 적용 (성공 시에만 교체)
        // -------------------------
        applyMonthlyInsightsByGpt(report);

        monthlyReportRepository.save(report);
    }

    // =========================================================
    // 주차별(1~4) 요약 생성: 1~7, 8~14, 15~21, 22~말일
    // activityRate 정책은 주간과 동일 (schedule & task 둘다 있으면 50/50)
    // =========================================================
    private List<MonthlyWeekSummary> buildWeekSummaries(
            MonthlyReport report,
            YearMonth ym,
            List<Todo> todos,
            List<Schedule> schedules,
            Map<Long, ScheduleResult> resultByScheduleId
    ) {
        List<WeekRange> ranges = splitTo4Weeks(ym);

        Map<LocalDate, List<Todo>> todosByDate = todos.stream()
                .filter(t -> t.getTargetDate() != null)
                .collect(Collectors.groupingBy(Todo::getTargetDate));

        // 날짜별 "확정된 결과"만 모음
        Map<LocalDate, List<ScheduleResult>> resultsByDate = new HashMap<>();
        for (Schedule s : schedules) {
            ScheduleResult r = resultByScheduleId.get(s.getId());
            if (r == null) continue;
            if (r.getStatus() == ScheduleResultStatus.PENDING) continue;
            if (r.getStatus() == ScheduleResultStatus.CANCELED) continue;

            LocalDate date = s.getStartTime().toLocalDate();
            resultsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(r);
        }

        List<MonthlyWeekSummary> list = new ArrayList<>();

        for (WeekRange range : ranges) {
            int taskCount = 0;
            int taskDone = 0;

            int scheduleCount = 0;
            int onTimeCount = 0;

            LocalDate cursor = range.startDate;
            while (!cursor.isAfter(range.endDate)) {

                List<Todo> dayTodos = todosByDate.getOrDefault(cursor, List.of());
                taskCount += dayTodos.size();
                taskDone += (int) dayTodos.stream().filter(Todo::isChecked).count();

                List<ScheduleResult> dayResults = resultsByDate.getOrDefault(cursor, List.of());
                scheduleCount += dayResults.size();
                onTimeCount += (int) dayResults.stream()
                        .filter(r -> r.getStatus() == ScheduleResultStatus.ON_TIME)
                        .count();

                cursor = cursor.plusDays(1);
            }

            BigDecimal taskRate = percent(taskDone, taskCount);
            BigDecimal punctualityRate = percent(onTimeCount, scheduleCount);

            BigDecimal activityRate;
            boolean hasSchedule = scheduleCount > 0;
            boolean hasTask = taskCount > 0;

            if (hasSchedule && hasTask) {
                activityRate = punctualityRate.multiply(BigDecimal.valueOf(0.5))
                        .add(taskRate.multiply(BigDecimal.valueOf(0.5)));
            } else if (hasSchedule) {
                activityRate = punctualityRate;
            } else if (hasTask) {
                activityRate = taskRate;
            } else {
                activityRate = BigDecimal.ZERO;
            }

            activityRate = activityRate.setScale(2, RoundingMode.HALF_UP);
            punctualityRate = punctualityRate.setScale(2, RoundingMode.HALF_UP);
            taskRate = taskRate.setScale(2, RoundingMode.HALF_UP);

            list.add(MonthlyWeekSummary.of(
                    report,
                    range.weekIndex,
                    scheduleCount,
                    taskCount,
                    punctualityRate,
                    taskRate,
                    activityRate
            ));
        }

        return list;
    }

    private List<WeekRange> splitTo4Weeks(YearMonth ym) {
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int lastDay = end.getDayOfMonth();

        List<WeekRange> ranges = new ArrayList<>(4);
        ranges.add(new WeekRange(1, start.withDayOfMonth(1), start.withDayOfMonth(Math.min(7, lastDay))));
        ranges.add(new WeekRange(2, start.withDayOfMonth(8), start.withDayOfMonth(Math.min(14, lastDay))));
        ranges.add(new WeekRange(3, start.withDayOfMonth(15), start.withDayOfMonth(Math.min(21, lastDay))));
        ranges.add(new WeekRange(4, start.withDayOfMonth(22), end));

        return ranges.stream()
                .filter(r -> !r.startDate.isAfter(r.endDate))
                .toList();
    }

    private static class WeekRange {
        private final int weekIndex;
        private final LocalDate startDate;
        private final LocalDate endDate;

        private WeekRange(int weekIndex, LocalDate startDate, LocalDate endDate) {
            this.weekIndex = weekIndex;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    // =========================================================
    // GPT: Monthly Insight Data String
    // =========================================================
    private String buildMonthlyInsightDataString(MonthlyReport report) {

        String weekCompact = report.getWeekSummaries().stream()
                .sorted(Comparator.comparing(MonthlyWeekSummary::getWeekIndex))
                .map(w -> "%d주차(일정%d/할일%d/활동%s%%)".formatted(
                        w.getWeekIndex(),
                        w.getScheduleCount(),
                        w.getTaskCount(),
                        w.getActivityRate().setScale(2, RoundingMode.HALF_UP).toPlainString()
                ))
                .collect(Collectors.joining(", "));

        String avgLate = (report.getAvgLateMinutes() == null) ? "없음" : report.getAvgLateMinutes() + "분";

        return """
            기간: %s ~ %s
            일정 정시율: %s%% (diff: %s)
            평균 지각: %s
            할일 완료율: %s%% (%d/%d) (diff: %s)
            활동일수: %d
            길찾기 보너스: %d
            생산성: %d / %s
            주차별 요약: %s
            """.formatted(
                report.getMonthStartDate(),
                report.getMonthEndDate(),
                report.getOnTimeRate().toPlainString(),
                report.getOnTimeDiff() == null ? "N/A" : report.getOnTimeDiff().toPlainString(),
                avgLate,
                report.getTaskCompletionRate().toPlainString(),
                report.getTaskDoneCount(),
                report.getTaskTotalCount(),
                report.getTaskCompletionDiff() == null ? "N/A" : report.getTaskCompletionDiff().toPlainString(),
                report.getActiveDays(),
                report.getNavigationBonus(),
                report.getProductivityScore(),
                report.getProductivityGrade(),
                weekCompact
        );
    }

    // =========================================================
    // GPT: Monthly Insights Apply (성공 시에만 교체)
    // =========================================================
    private void applyMonthlyInsightsByGpt(MonthlyReport report) {
        try {
            String dataString = buildMonthlyInsightDataString(report);
            DataPrompt dataPrompt = new DataPrompt("MONTHLY_REPORT_DATA", dataString);

            MonthlyInsightGptResponse gpt = monthlyInsightGptService.getGptResponse(
                    monthlyInsightPrompt,
                    dataPrompt,
                    700
            );

            if (gpt == null || gpt.insights() == null || gpt.insights().size() != 3) {
                throw new CustomException(MonthlyReportErrorCode.MONTHLY_REPORT_GPT_INVALID_RESPONSE);
            }

            List<MonthlyInsight> newInsights = new ArrayList<>();
            Set<MonthlyInsightType> used = new HashSet<>();

            for (MonthlyInsightGptResponse.InsightItem item : gpt.insights()) {
                MonthlyInsightType type;
                try {
                    type = MonthlyInsightType.valueOf(item.type());
                } catch (IllegalArgumentException ex) {
                    throw new CustomException(MonthlyReportErrorCode.MONTHLY_REPORT_GPT_INVALID_TYPE);
                }

                if (!used.add(type)) {
                    throw new CustomException(MonthlyReportErrorCode.MONTHLY_REPORT_GPT_INVALID_RESPONSE);
                }

                newInsights.add(
                        MonthlyInsight.of(
                                report,
                                type,
                                item.title(),
                                item.content(),
                                gpt.modelVersion()
                        )
                );
            }

            // 완전 성공일 때만 교체
            report.getInsights().clear();
            report.getInsights().addAll(newInsights);

        } catch (Exception e) {
            log.warn("monthly insight gpt failed. monthStart={}, reportId={}",
                    report.getMonthStartDate(), report.getId(), e);
        }
    }

    // =========================================================
    // Helpers (주간과 동일)
    // =========================================================
    private BigDecimal percent(int numerator, int denominator) {
        if (denominator <= 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal avgPunctualityScore(List<ScheduleResult> results) {
        if (results.isEmpty()) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal sum = results.stream()
                .map(r -> BigDecimal.valueOf(r.getPunctualityScore()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(results.size()), 2, RoundingMode.HALF_UP);
    }

    private Integer avgLateMinutes(List<ScheduleResult> results) {
        List<Integer> lateMinutes = results.stream()
                .filter(r -> r.getStatus() == ScheduleResultStatus.LATE)
                .map(ScheduleResult::getLateMinutes)
                .filter(Objects::nonNull)
                .toList();

        if (lateMinutes.isEmpty()) return null;

        long sum = 0;
        for (Integer m : lateMinutes) sum += m;
        return (int) Math.round((double) sum / lateMinutes.size());
    }

    private int calcActiveDays(List<Todo> todos, List<Schedule> schedules, LocalDate start, LocalDate end) {
        Set<LocalDate> days = new HashSet<>();

        for (Todo t : todos) {
            if (t.getTargetDate() != null) days.add(t.getTargetDate());
        }
        for (Schedule s : schedules) {
            days.add(s.getStartTime().toLocalDate());
        }

        days.removeIf(d -> d.isBefore(start) || d.isAfter(end));
        return days.size();
    }

    private int calcProductivityScore(BigDecimal pScore, BigDecimal tScore, int activeDays, int bonusB, int daysInMonth) {
        // Score = min(100, ((P*0.4)+(T*0.4)) * (1 + (D/daysInMonth)*0.2) + B)

        BigDecimal base = pScore.multiply(BigDecimal.valueOf(0.4))
                .add(tScore.multiply(BigDecimal.valueOf(0.4)));

        BigDecimal factor = BigDecimal.ONE.add(
                BigDecimal.valueOf(activeDays)
                        .divide(BigDecimal.valueOf(daysInMonth), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(0.2))
        );

        BigDecimal raw = base.multiply(factor).add(BigDecimal.valueOf(bonusB));
        BigDecimal capped = raw.min(BigDecimal.valueOf(100));

        return capped.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private ProductivityGrade toGrade(int score) {
        if (score >= 80) return ProductivityGrade.EXCELLENT;
        if (score >= 60) return ProductivityGrade.GOOD;
        if (score >= 40) return ProductivityGrade.FAIR;
        return ProductivityGrade.LOW;
    }
}

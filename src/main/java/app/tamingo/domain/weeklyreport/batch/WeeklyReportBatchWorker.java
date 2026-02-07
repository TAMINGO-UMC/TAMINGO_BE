package app.tamingo.domain.weeklyreport.batch;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.gpt.prompt.common.DataPrompt;
import app.tamingo.domain.gpt.prompt.weeklyreport.WeeklyReportInsightPrompt;
import app.tamingo.domain.gpt.service.weeklyreport.WeeklyInsightGptService;
import app.tamingo.domain.home.entity.enums.ArrivedStatus;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleResult;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.schedule.repository.ScheduleResultRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.weeklyreport.dto.WeeklyInsightsGptResponse;
import app.tamingo.domain.weeklyreport.entity.DailyActivitySummary;
import app.tamingo.domain.weeklyreport.entity.WeeklyInsight;
import app.tamingo.domain.weeklyreport.entity.WeeklyReport;
import app.tamingo.domain.weeklyreport.enums.DayOfWeekType;
import app.tamingo.domain.weeklyreport.enums.ProductivityGrade;
import app.tamingo.domain.weeklyreport.enums.WeeklyInsightType;
import app.tamingo.domain.weeklyreport.exception.WeeklyReportErrorCode;
import app.tamingo.domain.weeklyreport.repository.WeeklyReportRepository;
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
public class WeeklyReportBatchWorker {
    private final UserRepository userRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final WeeklyInsightGptService weeklyInsightGptService;
    private final TodoRepository todoRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleResultRepository scheduleResultRepository;
    private final WeeklyReportInsightPrompt weeklyReportInsightPrompt;

    /**
     * 유저 1명 리포트 생성은 유저 단위 트랜잭션으로 처리
     */
    /**
     * weekStartDate(월요일) ~ weekStartDate+6(일요일) 범위의
     * 주간 리포트를 유저에 대해 생성/갱신한다.
     *
     * - 멱등성 보장: (user_id, week_start_date) 유니크 기반으로 upsert
     * - dailySummaries는 orphanRemoval로 "교체" 방식
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generateOneUser(Long userId, LocalDate weekStartDate, LocalDate weekEndDate) {
        // -------------------------
        // 1) 원천 데이터 조회
        // -------------------------
        List<Todo> todos = todoRepository.findAllByUserIdAndTargetDateBetween(userId, weekStartDate, weekEndDate);

        LocalDateTime scheduleStart = weekStartDate.atStartOfDay();
        LocalDateTime scheduleEndExclusive = weekEndDate.plusDays(1).atStartOfDay();

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
                        (a, b) -> a // 혹시 중복이 나오면 첫 번째 값을 유지
                ));

        // 확정 결과만 집계 (PENDING/CANCELED 제외)
        List<ScheduleResult> finalizedResults = schedules.stream()
                .map(s -> resultByScheduleId.get(s.getId()))
                .filter(Objects::nonNull)
                .filter(r -> r.getStatus() != ArrivedStatus.PENDING)
                .filter(r -> r.getStatus() != ArrivedStatus.CANCELED)
                .toList();

        // -------------------------
        // 2) Todo 지표
        // -------------------------
        int taskTotal = todos.size();
        int taskDone = (int) todos.stream().filter(Todo::isChecked).count();
        BigDecimal taskCompletionRate = percent(taskDone, taskTotal);
        BigDecimal taskScore = taskCompletionRate; // 현재 정책상 동일 (확장 대비 분리)

        // -------------------------
        // 3) Schedule 지표 (P_rate / P_score / avgLate)
        // -------------------------
        int scheduleTotal = finalizedResults.size();

        int onTimeCount = (int) finalizedResults.stream()
                .filter(r -> r.getStatus() == ArrivedStatus.ON_TIME
                        || r.getStatus() == ArrivedStatus.EARLY)
                .count();

        BigDecimal onTimeRate = percent(onTimeCount, scheduleTotal);
        BigDecimal onTimeScore = avgPunctualityScore(finalizedResults);

        Integer avgLateMinutes = avgLateMinutes(finalizedResults);

        // -------------------------
        // 4) Navigation Bonus B
        // 정책: 길찾기 시작(navigationUsed=true) 후 도착(ON_TIME/EARLY/LATE)한 횟수당 +1
        // -------------------------
        int navigationBonus = (int) finalizedResults.stream()
                .filter(r -> Boolean.TRUE.equals(r.getNavigationUsed()))
                .filter(r ->
                        r.getStatus() == ArrivedStatus.ON_TIME
                        || r.getStatus() == ArrivedStatus.LATE
                        || r.getStatus() == ArrivedStatus.EARLY)
                .count();

        // -------------------------
        // 5) Active days D (0~7)
        // -------------------------
        int activeDays = calcActiveDays(todos, schedules, weekStartDate, weekEndDate);

        // -------------------------
        // 6) Productivity Score + Grade
        // -------------------------
        int productivityScore = calcProductivityScore(onTimeScore, taskScore, activeDays, navigationBonus);
        ProductivityGrade grade = toGrade(productivityScore);

        // -------------------------
        // 7) Diff (지난주 대비)
        // -------------------------
        LocalDate prevWeekStart = weekStartDate.minusWeeks(1);
        BigDecimal onTimeDiff = null;
        BigDecimal taskCompletionDiff = null;

        Optional<WeeklyReport> prevOpt =
                weeklyReportRepository.findByUserIdAndWeekStartDate(userId, prevWeekStart);

        if (prevOpt.isPresent()) {
            WeeklyReport prev = prevOpt.get();
            onTimeDiff = onTimeRate.subtract(prev.getOnTimeRate()).setScale(2, RoundingMode.HALF_UP);
            taskCompletionDiff = taskCompletionRate.subtract(prev.getTaskCompletionRate()).setScale(2, RoundingMode.HALF_UP);
        }


        // -------------------------
        // 8) WeeklyReport upsert
        // -------------------------
        Optional<WeeklyReport> reportOpt =
                weeklyReportRepository.findByUserIdAndWeekStartDate(userId, weekStartDate);

        WeeklyReport report;
        if (reportOpt.isPresent()) {
            // 기존 리포트면 업데이트
            report = reportOpt.get();
            report.updateMetrics(
                    weekEndDate,
                    onTimeRate,
                    onTimeScore,
                    onTimeDiff,
                    taskCompletionRate,
                    taskScore,
                    taskCompletionDiff,
                    taskDone,
                    taskTotal,
                    productivityScore,
                    grade,
                    activeDays,
                    navigationBonus,
                    avgLateMinutes
            );
        } else {
            User userRef = userRepository.getReferenceById(userId);
            // 없으면 생성
            report = WeeklyReport.of(
                    userRef,
                    weekStartDate,
                    weekEndDate,
                    onTimeRate,
                    onTimeScore,
                    onTimeDiff,
                    taskCompletionRate,
                    taskScore,
                    taskCompletionDiff,
                    taskDone,
                    taskTotal,
                    productivityScore,
                    grade,
                    activeDays,
                    navigationBonus,
                    avgLateMinutes
            );
        }


        // -------------------------
        // 9) DailyActivitySummary 7개 교체 생성
        // -------------------------
        List<DailyActivitySummary> newDailySummaries = buildDailySummaries(
                report,
                weekStartDate,
                todos,
                schedules,
                resultByScheduleId
        );

        report.getDailySummaries().clear();
        report.getDailySummaries().addAll(newDailySummaries);

        applyWeeklyInsightsByGpt(report);

        weeklyReportRepository.save(report);
    }

    // =========================================================
    // DailyActivitySummary 생성 (report FK 포함)
    // =========================================================
    private List<DailyActivitySummary> buildDailySummaries(
            WeeklyReport report,
            LocalDate weekStartDate,
            List<Todo> todos,
            List<Schedule> schedules,
            Map<Long, ScheduleResult> resultByScheduleId
    ) {
        Map<LocalDate, List<Todo>> todosByDate = todos.stream()
                .filter(t -> t.getTargetDate() != null)
                .collect(Collectors.groupingBy(Todo::getTargetDate));

        // 날짜별 "확정된 결과"만 모음
        Map<LocalDate, List<ScheduleResult>> resultsByDate = new HashMap<>();
        for (Schedule s : schedules) {
            ScheduleResult r = resultByScheduleId.get(s.getId());
            if (r == null) continue;
            if (r.getStatus() == ArrivedStatus.PENDING) continue;
            if (r.getStatus() == ArrivedStatus.CANCELED) continue;

            LocalDate date = s.getStartTime().toLocalDate();
            resultsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(r);
        }

        List<DailyActivitySummary> list = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStartDate.plusDays(i);
            DayOfWeekType dayType = toDayOfWeekType(date.getDayOfWeek());

            // todo
            List<Todo> dayTodos = todosByDate.getOrDefault(date, List.of());
            int taskCount = dayTodos.size();
            int taskDone = (int) dayTodos.stream().filter(Todo::isChecked).count();
            BigDecimal taskRate = percent(taskDone, taskCount);

            // schedule result
            List<ScheduleResult> dayResults = resultsByDate.getOrDefault(date, List.of());
            int scheduleCount = dayResults.size();
            int onTimeCount = (int) dayResults.stream()
                    .filter(r ->
                            r.getStatus() == ArrivedStatus.ON_TIME
                            || r.getStatus() == ArrivedStatus.EARLY)
                    .count();
            BigDecimal punctualityRate = percent(onTimeCount, scheduleCount);

            // activityRate policy:
            // - schedule & task: 50/50 weighted avg 스케줄 할일 있으면 평균냄
            // - only schedule: punctualityRate 스케줄만 있으면 스케줄 퍼센트냄
            // - only task: taskRate 할일만 있으면 할일만 퍼센트 냄
            // - none: 0.00 아무것도 없으면 0.00
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


            DailyActivitySummary summary = DailyActivitySummary.of(
                    report,
                    dayType,
                    scheduleCount,
                    taskCount,
                    punctualityRate,
                    taskRate,
                    activityRate
            );

            list.add(summary);
        }

        return list;
    }

    // =========================================================
    // Helper methods
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
                .filter(r -> r.getStatus() == ArrivedStatus.LATE)
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
        return days.size(); // 0~7
    }

    private int calcProductivityScore(BigDecimal pScore, BigDecimal tScore, int activeDays, int bonusB) {
        // Score = min(100, ((P*0.4)+(T*0.4)) * (1 + (D/7)*0.2) + B)

        BigDecimal base = pScore.multiply(BigDecimal.valueOf(0.4))
                .add(tScore.multiply(BigDecimal.valueOf(0.4)));

        BigDecimal factor = BigDecimal.ONE.add(
                BigDecimal.valueOf(activeDays)
                        .divide(BigDecimal.valueOf(7), 4, RoundingMode.HALF_UP)
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

    private DayOfWeekType toDayOfWeekType(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> DayOfWeekType.MONDAY;
            case TUESDAY -> DayOfWeekType.TUESDAY;
            case WEDNESDAY -> DayOfWeekType.WEDNESDAY;
            case THURSDAY -> DayOfWeekType.THURSDAY;
            case FRIDAY -> DayOfWeekType.FRIDAY;
            case SATURDAY -> DayOfWeekType.SATURDAY;
            case SUNDAY -> DayOfWeekType.SUNDAY;
        };
    }
    //gpt
    private String buildWeeklyInsightDataString(WeeklyReport report) {

        String dailyCompact = report.getDailySummaries().stream()
                .sorted(Comparator.comparing(DailyActivitySummary::getDayOfWeek))
                .map(d -> "%s(일정%d/할일%d/활동%s%%)".formatted(
                        d.getDayOfWeek(),
                        d.getScheduleCount(),
                        d.getTaskCount(),
                        d.getActivityRate().setScale(2, RoundingMode.HALF_UP).toPlainString()
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
            요일별 요약: %s
            """.formatted(
                report.getWeekStartDate(),
                report.getWeekEndDate(),
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
                dailyCompact
        );
    }

    //gpt
    private void applyWeeklyInsightsByGpt(WeeklyReport report) {
        try {
            String dataString = buildWeeklyInsightDataString(report);
            DataPrompt dataPrompt = new DataPrompt("WEEKLY_REPORT_DATA", dataString);

            WeeklyInsightsGptResponse gpt = weeklyInsightGptService.getGptResponse(
                    weeklyReportInsightPrompt,
                    dataPrompt,
                    700
            );

            if (gpt == null || gpt.insights() == null || gpt.insights().size() != 3) {
                throw new CustomException(WeeklyReportErrorCode.WEEKLY_REPORT_GPT_INVALID_RESPONSE);
            }

            List<WeeklyInsight> newInsights = new ArrayList<>();
            Set<WeeklyInsightType> used = new HashSet<>();

            for (WeeklyInsightsGptResponse.InsightItem item : gpt.insights()) {
                WeeklyInsightType type;
                try {
                    type = WeeklyInsightType.valueOf(item.type());
                } catch (IllegalArgumentException ex) {
                    throw new CustomException(WeeklyReportErrorCode.WEEKLY_REPORT_GPT_INVALID_TYPE);
                }

                if (!used.add(type)) {
                    throw new CustomException(WeeklyReportErrorCode.WEEKLY_REPORT_GPT_INVALID_RESPONSE);
                }

                newInsights.add(
                        WeeklyInsight.of(
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
            log.warn("weekly insight gpt failed. weekStart={}, reportId={}",
                    report.getWeekStartDate(), report.getId(), e);
        }
    }


}

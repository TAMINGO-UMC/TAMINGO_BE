package app.tamingo.domain.monthlyreport.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.monthlyreport.dto.MonthlyInsightResponse;
import app.tamingo.domain.monthlyreport.dto.MonthlyReportResponse;
import app.tamingo.domain.monthlyreport.dto.WeeklyActivityResponse;
import app.tamingo.domain.monthlyreport.entity.MonthlyInsight;
import app.tamingo.domain.monthlyreport.entity.MonthlyReport;
import app.tamingo.domain.monthlyreport.exception.MonthlyReportErrorCode;
import app.tamingo.domain.monthlyreport.repository.MonthlyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {

    private final MonthlyReportRepository monthlyReportRepository;

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Long userId, String yearMonth) {
        YearMonth ym = parseYearMonth(yearMonth);

        LocalDate monthStart = ym.atDay(1);

        MonthlyReport report = monthlyReportRepository
                .findWithWeekSummariesByUserIdAndMonthStartDate(userId, monthStart)
                .orElseThrow(() -> new CustomException(MonthlyReportErrorCode.MONTHLY_REPORT_NOT_FOUND));

        // insights는 별도 fetch로 초기화 (MultipleBagFetch 방지)
        monthlyReportRepository.findWithInsightsByUserIdAndMonthStartDate(userId, monthStart)
                .orElseThrow(() -> new CustomException(MonthlyReportErrorCode.MONTHLY_REPORT_NOT_FOUND));

        List<WeeklyActivityResponse> weeklyActivities = report.getWeekSummaries().stream()
                .sorted(Comparator.comparingInt(ws -> ws.getWeekIndex()))
                .map(ws -> new WeeklyActivityResponse(
                        ws.getWeekIndex(),
                        ws.getScheduleCount(),
                        ws.getTaskCount(),
                        ws.getActivityRate()
                ))
                .toList();

        List<MonthlyInsightResponse> insights = report.getInsights().stream()
                // 타입 순서대로 정렬
                .sorted(Comparator.comparingInt(i -> i.getType().ordinal()))
                .map(this::toMonthlyInsightResponse)
                .toList();

        return new MonthlyReportResponse(
                report.getMonthStartDate(),
                report.getMonthEndDate(),

                report.getOnTimeRate(),
                report.getOnTimeDiff(),

                report.getTaskCompletionRate(),
                report.getTaskCompletionDiff(),
                report.getTaskTotalCount(),
                report.getTaskDoneCount(),

                report.getProductivityScore(),
                report.getProductivityGrade(),

                weeklyActivities,

                //ai 인사이트
                insights
        );
    }

    private YearMonth parseYearMonth(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth); // "2026-01"
        } catch (Exception e) {
            throw new CustomException(MonthlyReportErrorCode.MONTHLY_REPORT_INVALID_REQUEST);
        }
    }
    private MonthlyInsightResponse toMonthlyInsightResponse(MonthlyInsight i) {
        return new MonthlyInsightResponse(
                i.getType(),
                i.getTitle(),
                i.getContent(),
                i.getModelVersion()
        );
    }
}

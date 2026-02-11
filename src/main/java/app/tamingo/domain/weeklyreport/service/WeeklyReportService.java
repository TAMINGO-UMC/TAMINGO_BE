package app.tamingo.domain.weeklyreport.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.weeklyreport.dto.DailyActivityResponse;
import app.tamingo.domain.weeklyreport.dto.WeeklyInsightResponse;
import app.tamingo.domain.weeklyreport.dto.WeeklyReportResponse;
import app.tamingo.domain.weeklyreport.entity.DailyActivitySummary;
import app.tamingo.domain.weeklyreport.entity.WeeklyInsight;
import app.tamingo.domain.weeklyreport.entity.WeeklyReport;
import app.tamingo.domain.weeklyreport.exception.WeeklyReportErrorCode;
import app.tamingo.domain.weeklyreport.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(Long userId, LocalDate weekStartDate) {

        WeeklyReport report = weeklyReportRepository
                .findWithDailySummariesByUserIdAndWeekStartDate(userId, weekStartDate)
                .orElseThrow(() -> new CustomException(WeeklyReportErrorCode.WEEKLY_REPORT_NOT_FOUND));

        // 같은 트랜잭션 안에서 insights만 따로 로딩(초기화)
        weeklyReportRepository.findWithInsightsByUserIdAndWeekStartDate(userId, weekStartDate)
                .orElseThrow(() -> new CustomException(WeeklyReportErrorCode.WEEKLY_REPORT_NOT_FOUND));

        report.getInsights().size();

        List<DailyActivityResponse> dailyActivities = report.getDailySummaries().stream()
                .sorted(Comparator.comparingInt(s -> s.getDayOfWeek().ordinal()))
                .map(this::toDailyActivityResponse)
                .toList();

        List<WeeklyInsightResponse> insights = report.getInsights().stream()
                // 타입 순서대로 정렬
                .sorted(Comparator.comparingInt(i -> i.getType().ordinal()))
                .map(this::toWeeklyInsightResponse)
                .toList();

        return new WeeklyReportResponse(
                report.getWeekStartDate(),
                report.getWeekEndDate(),

                // 정시
                report.getOnTimeRate(),
                report.getOnTimeDiff(),

                // todo(=task)
                report.getTaskCompletionRate(),
                report.getTaskCompletionDiff(),
                report.getTaskTotalCount(),
                report.getTaskDoneCount(),

                // 생산성
                report.getProductivityScore(),
                report.getProductivityGrade(),

                // 요일별
                dailyActivities,

                //ai 인사이트
                insights
        );
    }

    private DailyActivityResponse toDailyActivityResponse(DailyActivitySummary s) {
        return new DailyActivityResponse(
                s.getDayOfWeek(),
                s.getScheduleCount(),
                s.getTaskCount(),
                s.getActivityRate()
        );
    }

    private WeeklyInsightResponse toWeeklyInsightResponse(WeeklyInsight i) {
        return new WeeklyInsightResponse(
                i.getType(),
                i.getTitle(),
                i.getContent(),
                i.getModelVersion(),
                i.getType().emoji()
        );
    }
}

package app.tamingo.domain.weeklyreport.dto;

import app.tamingo.domain.weeklyreport.enums.ProductivityGrade;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WeeklyReportResponse(
        LocalDate weekStartDate,
        LocalDate weekEndDate,

        // 정시 도착률 (+diff)
        BigDecimal onTimeRate,
        BigDecimal onTimeDiff,

        // todo(=task)
        BigDecimal taskCompletionRate,
        BigDecimal taskCompletionDiff,
        Integer taskTotalCount,
        Integer taskDoneCount,

        // 생산성
        Integer productivityScore,
        ProductivityGrade productivityGrade,

        // 요일별 탭
        List<DailyActivityResponse> dailyActivities,

        //ai 인사이트
        List<WeeklyInsightResponse> insights
) {
}

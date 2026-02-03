package app.tamingo.domain.monthlyreport.dto;

import app.tamingo.domain.weeklyreport.enums.ProductivityGrade;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MonthlyReportResponse(
        LocalDate monthStartDate,
        LocalDate monthEndDate,

        BigDecimal onTimeRate,
        BigDecimal onTimeDiff,

        BigDecimal taskCompletionRate,
        BigDecimal taskCompletionDiff,
        Integer taskTotalCount,
        Integer taskDoneCount,

        Integer productivityScore,
        ProductivityGrade productivityGrade,

        List<WeeklyActivityResponse> weeklyActivities,

        //ai 인사이트
        List<MonthlyInsightResponse> insights
) {}

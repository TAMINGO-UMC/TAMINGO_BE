package app.tamingo.domain.mypage.dto;

import app.tamingo.domain.calendar.enums.CalendarIntegrationStatus;
import app.tamingo.domain.weeklyreport.enums.ProductivityGrade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 마이페이지 화면에 필요한 요약 데이터 응답 DTO
 */
public record MyPageSummaryResponse(
        Profile profile,
        WeeklyReportSummary weeklyReport,
        Counts counts,
        CalendarIntegration integration,
        Settings settings
) {

    public record Profile(
            String nickname,
            String email
    ) {
    }

    /**
     * 주간 리포트 카드 요약(없으면 null)
     */
    public record WeeklyReportSummary(
            LocalDate weekStartDate,
            LocalDate weekEndDate,

            BigDecimal onTimeRate,
            BigDecimal onTimeDiff,

            BigDecimal taskCompletionRate,
            Integer taskDoneCount,
            Integer taskTotalCount,

            Integer productivityScore,
            ProductivityGrade productivityGrade
    ) {
    }

    public record Counts(
            long scheduleCategoryCount,
            long todoCategoryCount,
            long favoritePlaceCount
    ) {
    }

    /**
     * 애플 캘린더 연동 요약
     */
    public record CalendarIntegration(
            boolean linked,
            boolean syncFromApple,
            CalendarIntegrationStatus status
    ) {
    }

    /**
     * 화면에 표시되는 주요 설정값 요약
     */
    public record Settings(
            LocalTime activeStartTime,
            LocalTime activeEndTime,
            List<String> transportPriority,
            boolean importantAlarmEnabled,
            boolean learningDataEnabled
    ) {
    }
}

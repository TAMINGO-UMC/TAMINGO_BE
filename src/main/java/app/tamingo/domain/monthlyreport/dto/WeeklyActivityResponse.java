package app.tamingo.domain.monthlyreport.dto;

import java.math.BigDecimal;

public record WeeklyActivityResponse(
        Integer weekIndex,      // 1~4
        Integer scheduleCount,
        Integer taskCount,
        BigDecimal activityRate
) {}

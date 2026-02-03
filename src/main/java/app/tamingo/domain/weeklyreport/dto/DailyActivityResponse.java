package app.tamingo.domain.weeklyreport.dto;

import app.tamingo.domain.weeklyreport.enums.DayOfWeekType;
import java.math.BigDecimal;

public record DailyActivityResponse(
        DayOfWeekType dayOfWeek,
        Integer scheduleCount,
        Integer taskCount,
        BigDecimal activityRate
) {
}

package app.tamingo.domain.home.entity.enums;

public enum TimeSlot {
    EARLY_MORNING,
    MORNING,
    DAY,
    EVENING,
    NIGHT,
    LATE_NIGHT

    ;

    public static TimeSlot fromHour(int hour) {
        if (hour >= 5 && hour < 8) {
            return EARLY_MORNING;
        }
        if (hour >= 8 && hour < 12) {
            return MORNING;
        }
        if (hour >= 12 && hour < 18) {
            return DAY;
        }
        if (hour >= 18 && hour < 22) {
            return EVENING;
        }
        if (hour >= 22 || hour < 1) {
            return NIGHT;
        }
        return LATE_NIGHT;
    }
}

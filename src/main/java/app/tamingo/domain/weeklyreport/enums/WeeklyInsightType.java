package app.tamingo.domain.weeklyreport.enums;

public enum WeeklyInsightType {
    PRODUCTIVITY,    // 생산성
    TIME_MANAGEMENT, // 시간 관리
    CONSISTENCY,     // 꾸준함
    FOCUS,           // 집중
    HABIT;           // 습관

    public String emoji() {
        return switch (this) {
            case PRODUCTIVITY -> "🚀";
            case TIME_MANAGEMENT -> "⏰";
            case CONSISTENCY -> "🔁";
            case FOCUS -> "🎯";
            case HABIT -> "🌱";
        };
    }
}

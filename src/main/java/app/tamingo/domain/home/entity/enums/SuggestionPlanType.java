package app.tamingo.domain.home.entity.enums;

public enum SuggestionPlanType {
    TODO_BASED_TODO,
    TODO_BASED_SCHEDULE,
    SCHEDULE_BASED_SCHEDULE;

    public boolean isTodoBased() {
        return this == TODO_BASED_TODO || this == TODO_BASED_SCHEDULE;
    }
}

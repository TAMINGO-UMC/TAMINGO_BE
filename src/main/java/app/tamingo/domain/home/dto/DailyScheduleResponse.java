package app.tamingo.domain.home.dto;

import app.tamingo.domain.home.entity.enums.CurrentStatus;

import java.time.LocalTime;
import java.util.List;

public record DailyScheduleResponse(
        ScheduleStatusResponse scheduleStatus,
        List<LinkedTodoResponse> linkedTodos,
        List<RecommendationResponse> routeDetourRecommendations
) {
    public record ScheduleStatusResponse(
            CurrentStatus currentStatus,
            boolean isStarted,
            Integer leftOrDelayMinutes,
            LocalTime expectedDepartureTime,
            LocalTime expectedArrivalTime,
            Integer lateArrivalMinutes
    ) {
    }

    public record RecommendationResponse(
            Long suggestionId,
            String suggestionTitle,
            String placeName,
            String detourMinutes,
            String recommendationMessage
    ) {
    }

    public record LinkedTodoResponse(
            Long todoId,
            String title,
            String placeName
    ) {
    }
}

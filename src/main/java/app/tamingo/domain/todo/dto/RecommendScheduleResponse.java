package app.tamingo.domain.todo.dto;

import app.tamingo.domain.schedule.dto.ScheduleSummaryResponse;

import java.util.List;

public record RecommendScheduleResponse(
        List<ScheduleSummaryResponse> nearbyTodos,
        List<ScheduleSummaryResponse> candidateTodos,
        boolean isFavoriteRecommendation
) {}

package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.todo.dto.TodoSummaryResponse;
import lombok.Builder;
import java.util.List;

// 2km 범위 내 할 일+조건 기반 할 일+자주 가는 장소 추가 여부
@Builder
public record RecommendTodoResponse(
        List<TodoSummaryResponse> nearbyTodos,
        List<TodoSummaryResponse> candidateTodos,
        boolean isFavoriteRecommendation
) {}

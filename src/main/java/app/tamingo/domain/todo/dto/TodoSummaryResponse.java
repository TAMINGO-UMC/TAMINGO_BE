package app.tamingo.domain.todo.dto;

import app.tamingo.domain.todo.entity.Todo;

public record TodoSummaryResponse(
        Long todoId,
        String title,
        String placeName
) {
    public static TodoSummaryResponse from(Todo todo){
        return new TodoSummaryResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getPlaceName()
        );
    }
}

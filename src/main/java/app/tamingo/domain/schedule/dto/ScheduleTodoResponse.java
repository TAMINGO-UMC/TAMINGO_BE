package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.todo.entity.Todo;

public record ScheduleTodoResponse(

        Long todoId,
        String title,
        String placeName

) {
    public static ScheduleTodoResponse from(Todo todo){
        return new ScheduleTodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getPlaceName()
        );
    }
}

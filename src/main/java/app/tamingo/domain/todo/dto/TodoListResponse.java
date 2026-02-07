package app.tamingo.domain.todo.dto;

import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoCategory;

public record TodoListResponse(
        Long todoId,
        String title,
        String categoryName,
        String categoryColor,
        boolean isChecked
) {
    public static TodoListResponse from(Todo todo) {
        TodoCategory category = todo.getTodoCategory();

        String name = (category != null) ? category.getName() : null;
        String color = (category != null) ? category.getColorCode() : null;

        return new TodoListResponse(
                todo.getId(),
                todo.getTitle(),
                name,
                color,
                todo.isChecked()
        );
    }
}

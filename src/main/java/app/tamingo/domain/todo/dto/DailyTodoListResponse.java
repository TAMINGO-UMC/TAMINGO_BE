package app.tamingo.domain.todo.dto;

import java.util.List;

public record DailyTodoListResponse(
        List<TodoListResponse> dailyTodos,
        List<TodoListResponse> backlogTodos
) {
    public static DailyTodoListResponse of(
            List<TodoListResponse> dailyTodos,
            List<TodoListResponse> backlogTodos
    ) {
        return new DailyTodoListResponse(dailyTodos, backlogTodos);
    }
}

package app.tamingo.domain.todo.dto;

import app.tamingo.domain.todo.entity.TodoCategory;

public record TodoCategoryResponse(
        Long id, //카테고리 아이디
        String name, //이름
        String colorCode  // 색상 코드
) {
    public static TodoCategoryResponse from(TodoCategory c) {
        return new TodoCategoryResponse(
                c.getId(),
                c.getName(),
                c.getColorCode()
        );
    }
}

package app.tamingo.domain.todo.dto;

public record AiTodoInfo(
        String category,
        String placeName,
        String address,
        Double latitude,
        Double longitude,
        Integer duration
) {}

package app.tamingo.domain.todo.dto;

public record RecommendScheduleRequest(
        String placeName,
        String address,
        Double latitude,
        Double longitude
) {}

package app.tamingo.domain.schedule.dto;

public record RecommendTodoRequest(
        String placeName,
        String address,
        Double latitude,
        Double longitude
) {}

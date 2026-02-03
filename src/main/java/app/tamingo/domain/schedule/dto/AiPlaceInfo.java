package app.tamingo.domain.schedule.dto;

public record AiPlaceInfo(
        String placeName,
        String address,
        Double latitude,
        Double longitude,
        String category
) {}

package app.tamingo.domain.home.dto;

public record StartLocationGpsResponse(
        boolean overridden,
        String reason,
        Integer snapshotMinutes,
        Integer gpsMinutes,
        Double usedStartLat,
        Double usedStartLng
) {}

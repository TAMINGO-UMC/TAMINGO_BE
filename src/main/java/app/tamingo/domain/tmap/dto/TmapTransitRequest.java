package app.tamingo.domain.tmap.dto;

public record TmapTransitRequest(
        String startX,
        String startY,
        String endX,
        String endY,
        int count,
        int lang,
        String format
) {}

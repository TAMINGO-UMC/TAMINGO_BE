package app.tamingo.domain.userlearning.dto;

public record UserSummaryResponse(
        Long patternCount,
        double avgAccuracy,
        int fvpCount
) {}

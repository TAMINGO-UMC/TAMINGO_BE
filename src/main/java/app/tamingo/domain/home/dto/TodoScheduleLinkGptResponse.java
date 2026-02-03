package app.tamingo.domain.home.dto;

public record TodoScheduleLinkGptResponse(
        boolean linked,
        Long scheduleId,
        String aiComment,
        Long categoryId,
        String categoryName
) {}

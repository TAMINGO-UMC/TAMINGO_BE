package app.tamingo.domain.weeklyreport.dto;

import app.tamingo.domain.weeklyreport.enums.WeeklyInsightType;

public record WeeklyInsightResponse(
        WeeklyInsightType type,
        String title,
        String content,
        String modelVersion
) {}

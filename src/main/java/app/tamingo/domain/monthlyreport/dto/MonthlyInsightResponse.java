package app.tamingo.domain.monthlyreport.dto;

import app.tamingo.domain.monthlyreport.enums.MonthlyInsightType;

public record MonthlyInsightResponse(
        MonthlyInsightType type,
        String title,
        String content,
        String modelVersion
) {}
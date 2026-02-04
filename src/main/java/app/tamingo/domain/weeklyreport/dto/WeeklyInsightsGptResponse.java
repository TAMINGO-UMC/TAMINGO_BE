package app.tamingo.domain.weeklyreport.dto;

import java.util.List;

public record WeeklyInsightsGptResponse(
        List<InsightItem> insights,
        String modelVersion
) {
    public record InsightItem(
            String type,
            String title,
            String content
    ) {}
}

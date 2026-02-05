package app.tamingo.domain.monthlyreport.dto;

import java.util.List;

public record MonthlyInsightGptResponse(
        List<InsightItem> insights,
        String modelVersion
) {
    public record InsightItem(
            String type,
            String title,
            String content
    ) {}
}

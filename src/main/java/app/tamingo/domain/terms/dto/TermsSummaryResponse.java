package app.tamingo.domain.terms.dto;

import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;

public record TermsSummaryResponse(
        TermsCode code,
        String title,
        boolean isRequired
) {
    public static TermsSummaryResponse from(Terms terms) {
        return new TermsSummaryResponse(
                terms.getCode(),
                terms.getTitle(),
                terms.isRequired()
        );
    }
}
package app.tamingo.domain.terms.dto;

import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;

public record TermsSummaryResponse(
        TermsCode code,
        String codeDescription,
        String title,
        boolean required
) {
    public static TermsSummaryResponse from(Terms terms) {
        return new TermsSummaryResponse(
                terms.getCode(),
                terms.getCode().getDescription(),
                terms.getTitle(),
                terms.isRequired()
        );
    }
}
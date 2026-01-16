package app.tamingo.domain.terms.dto;

import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;

import java.time.LocalDateTime;

public record TermsDetailResponse(
        TermsCode code,
        String title,
        String content,
        boolean isRequired,
        String version,
        LocalDateTime effectiveAt
) {
    public static TermsDetailResponse from(Terms terms) {
        return new TermsDetailResponse(
                terms.getCode(),
                terms.getTitle(),
                terms.getContent(),
                terms.isRequired(),
                terms.getVersion(),
                terms.getEffectiveAt()
        );
    }
}
package app.tamingo.domain.auth.dto.signup;

import app.tamingo.domain.terms.entity.TermsCode;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateSignupSessionRequest(
        @NotNull Map<TermsCode, Boolean> agreedTerms
) { }
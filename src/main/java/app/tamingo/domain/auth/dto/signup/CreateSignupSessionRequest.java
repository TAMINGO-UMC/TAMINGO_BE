package app.tamingo.domain.auth.dto.signup;

import app.tamingo.domain.terms.entity.TermsCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateSignupSessionRequest(
        @Schema(
                description = "약관 동의 여부 (TermsCode 기준)",
                example = """
                {
                  "SERVICE": true,
                  "PRIVACY": true,
                  "AI_SERVICE": false,
                  "LOCATION": false,
                  "MARKETING": false
                }
                """
        )
        Map<TermsCode, Boolean> terms
) {
}
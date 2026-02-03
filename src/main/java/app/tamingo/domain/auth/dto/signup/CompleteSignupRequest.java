package app.tamingo.domain.auth.dto.signup;

import jakarta.validation.constraints.NotBlank;

public record CompleteSignupRequest(
        @NotBlank String nickname,
        @NotBlank String password
) {}
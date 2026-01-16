package app.tamingo.domain.auth.dto.signup;

public record VerifyEmailCodeRequest(
        String signupSessionId,
        String email,
        String code
) {}
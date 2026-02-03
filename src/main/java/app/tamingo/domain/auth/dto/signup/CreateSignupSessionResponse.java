package app.tamingo.domain.auth.dto.signup;

public record CreateSignupSessionResponse(
        String signupSessionId,
        long expiresInSec
) { }
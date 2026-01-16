package app.tamingo.domain.auth.dto.signup;

public record CompleteSignupRequest(
        String signupSessionId,
        String nickname
) {}
package app.tamingo.domain.auth.dto.signup;

public record VerifyEmailCodeRequest(
        String email,
        String code
) {}
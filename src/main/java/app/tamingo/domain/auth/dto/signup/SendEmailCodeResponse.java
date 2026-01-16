package app.tamingo.domain.auth.dto.signup;

public record SendEmailCodeResponse(
        long expiresInSec
) {
}
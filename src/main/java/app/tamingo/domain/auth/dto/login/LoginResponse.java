package app.tamingo.domain.auth.dto.login;

public record LoginResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {}
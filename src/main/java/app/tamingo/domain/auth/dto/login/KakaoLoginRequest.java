package app.tamingo.domain.auth.dto.login;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank String kakaoAccessToken
) {}
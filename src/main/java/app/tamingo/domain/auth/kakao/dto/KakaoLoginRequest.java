package app.tamingo.domain.auth.kakao.dto;

import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank String kakaoAccessToken
) {}
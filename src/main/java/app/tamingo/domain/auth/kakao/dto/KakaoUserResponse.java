package app.tamingo.domain.auth.kakao.dto;

public record KakaoUserResponse(
        String id,
        KakaoAccount kakao_account
) {
    public record KakaoAccount(
            String email
    ) {}
}
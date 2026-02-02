package app.tamingo.domain.auth.kakao.client;

import app.tamingo.common.webclient.ApiClient;
import app.tamingo.domain.auth.kakao.dto.KakaoUserResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class KakaoLoginClient extends ApiClient {

    public KakaoLoginClient(
            @Qualifier("kakaoUserWebClient") WebClient webClient
    ) {
        super(webClient);
    }

    public KakaoUserResponse getMe(String kakaoAccessToken) {
        return get(
                uri -> uri.path("/v2/user/me").build(),
                KakaoUserResponse.class,
                headers -> headers.setBearerAuth(kakaoAccessToken)
        );
    }
}
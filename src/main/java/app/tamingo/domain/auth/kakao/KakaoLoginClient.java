package app.tamingo.domain.auth.kakao;

import app.tamingo.common.webclient.ApiClient;
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

    public KakaoUserResponse getUserInfo(String kakaoAccessToken) {
        return get(
                uri -> uri.path("/v2/user/me").build(),
                KakaoUserResponse.class,
                headers -> headers.setBearerAuth(kakaoAccessToken)
        );
    }
}
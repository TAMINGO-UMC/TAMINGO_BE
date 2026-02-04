package app.tamingo.domain.kakao.client;

import app.tamingo.common.webclient.ApiClient;
import app.tamingo.domain.kakao.dto.KakaoGeoResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class KakaoMapGeoClient extends ApiClient {

    public KakaoMapGeoClient(@Qualifier("kakaoMapGeoWebClient") WebClient webClient) {
        super(webClient);
    }

    public KakaoGeoResponseDto coordToAddress(double lon, double lat) {
        try {
            return get(
                    uriBuilder -> uriBuilder
                            .path("/v2/local/geo/coord2address.json")
                            .queryParam("x", lon)
                            .queryParam("y", lat)
                            .build(),
                    KakaoGeoResponseDto.class
            );
        } catch (Exception e) {
            log.error("Kakao coord2address failed: {}", e.getMessage());
            return null;
        }
    }
}

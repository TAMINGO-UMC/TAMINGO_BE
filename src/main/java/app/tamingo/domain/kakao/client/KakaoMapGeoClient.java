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
        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            log.warn("Kakao coord2address skipped: invalid coordinates lon={}, lat={}", lon, lat);
            return null;
        }
        try {
            KakaoGeoResponseDto response = get(
                    uriBuilder -> uriBuilder
                            .path("/v2/local/geo/coord2address.json")
                            .queryParam("x", lon)
                            .queryParam("y", lat)
                            .build(),
                    KakaoGeoResponseDto.class
            );
            if (response == null) {
                log.warn("Kakao coord2address returned null. lon={}, lat={}", lon, lat);
            }
            return response;
        } catch (Exception e) {
            log.error("Kakao coord2address failed: {}", e.getMessage());
            return null;
        }
    }
}

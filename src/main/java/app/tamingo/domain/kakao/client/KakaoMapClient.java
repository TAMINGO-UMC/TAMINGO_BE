package app.tamingo.domain.kakao.client;

import app.tamingo.common.webclient.ApiClient;
import app.tamingo.domain.kakao.dto.KakaoPlaceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
public class KakaoMapClient extends ApiClient {

    public KakaoMapClient(@Qualifier("kakaoWebClient") WebClient webClient) {
        super(webClient);
    }

    public KakaoPlaceDto searchPlace(String query) {
        if (query == null || query.isBlank()) return null;

        try {
            KakaoResponse response = get(
                    uriBuilder -> uriBuilder
                            .path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .build(),
                    KakaoResponse.class
            );

            if (response != null && response.documents() != null && !response.documents().isEmpty()) {
                KakaoResponse.Document doc = response.documents().get(0);

                // [핵심 로직] 도로명 주소(road_address_name)가 있으면 그걸 쓰고, 없으면 지번 주소(address_name) 사용
                String finalAddress = (doc.road_address_name != null && !doc.road_address_name.isBlank())
                        ? doc.road_address_name
                        : doc.address_name;

                log.info("Kakao Search: '{}' -> 주소: '{}' ({}, {})", query, finalAddress, doc.y, doc.x);

                return new KakaoPlaceDto(
                        doc.place_name,
                        finalAddress, // 여기서 결정된 주소가 들어감
                        Double.parseDouble(doc.y),
                        Double.parseDouble(doc.x)
                );
            }
        } catch (Exception e) {
            log.error("Kakao API Failed: {}", e.getMessage());
        }
        return null;
    }

    // 카카오 응답 매핑용 내부 DTO
    record KakaoResponse(List<Document> documents) {
        record Document(
                String place_name,
                String address_name,      // 지번 주소
                String road_address_name, // [NEW] 도로명 주소 필드 추가!
                String x,
                String y
        ) {}
    }

}

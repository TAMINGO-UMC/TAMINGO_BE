package app.tamingo.domain.kakao.service;

import app.tamingo.domain.kakao.client.KakaoMapClient;
import app.tamingo.domain.kakao.dto.KakaoPlaceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoSearchService {

    private final KakaoMapClient kakaoMapClient;

    /**
     * 키워드(장소명)로 장소를 검색하여 정확한 주소와 좌표를 반환합니다.
     * 검색 결과가 없으면 null을 반환합니다.
     */
    public KakaoPlaceDto search(String keyword) {
        // 1. 키워드 유효성 체크
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        // 2. 클라이언트 호출 (로그는 클라이언트 내부에서도 찍지만, 서비스 흐름 확인용으로 추가 가능)
        KakaoPlaceDto result = kakaoMapClient.searchPlace(keyword);

        if (result != null) {
            log.info("Kakao Service Resolved: '{}' -> {}", keyword, result.address());
        } else {
            log.warn("Kakao Service Failed to find: '{}'", keyword);
        }

        return result;
    }

}

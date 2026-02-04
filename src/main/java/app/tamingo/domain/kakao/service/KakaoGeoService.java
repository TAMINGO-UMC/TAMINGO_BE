package app.tamingo.domain.kakao.service;

import app.tamingo.domain.kakao.client.KakaoMapGeoClient;
import app.tamingo.domain.kakao.dto.KakaoAddressResponseDto;
import app.tamingo.domain.kakao.dto.KakaoGeoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoGeoService {

    private final KakaoMapGeoClient kakaoMapGeoClient;

    public KakaoAddressResponseDto getAddress(double lon, double lat) {
        KakaoGeoResponseDto response = kakaoMapGeoClient.coordToAddress(lon, lat);

        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            return null;
        }

        KakaoGeoResponseDto.Document doc = response.documents().get(0);

        // 도로명 우선, 없으면 지번
        String addressName = doc.road_address() != null
                ? doc.road_address().address_name()
                : doc.address() != null
                ? doc.address().address_name()
                : null;

        if (addressName == null) return null;

        return new KakaoAddressResponseDto(
                addressName,
                doc.address() != null ? doc.address().region_1depth_name() : null,
                doc.address() != null ? doc.address().region_2depth_name() : null,
                doc.address() != null ? doc.address().region_3depth_name() : null
        );
    }
}

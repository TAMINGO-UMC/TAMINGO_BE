package app.tamingo.domain.kakao.dto;

import java.util.List;

public record KakaoGeoResponseDto(
        Meta meta,
        List<Document> documents
) {
    public record Meta(
            Integer total_count
    ) {}

    public record Document(
            Address address,
            RoadAddress road_address
    ) {}

    public record Address(
            String address_name,
            String region_1depth_name,
            String region_2depth_name,
            String region_3depth_name
    ) {}

    public record RoadAddress(
            String address_name,
            String road_name,
            String building_name,
            String zone_no
    ) {}
}

package app.tamingo.domain.kakao.dto;

public record KakaoAddressResponseDto(
        String addressName,     // 최종 주소
        String region1,
        String region2,
        String region3
) {}
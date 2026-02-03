package app.tamingo.domain.kakao.dto;

public record KakaoPlaceDto(
        String placeName,
        String address,
        Double latitude,
        Double longitude
) {}

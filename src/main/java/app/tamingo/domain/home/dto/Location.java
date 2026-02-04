package app.tamingo.domain.home.dto;

/**
 * 위도/경도 좌표를 표현하는 간단한 DTO
 * 경로 polyline 정보를 담기 위해 사용
 */
public record Location(
        double latitude,
        double longitude) {
}

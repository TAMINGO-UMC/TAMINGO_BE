package app.tamingo.domain.home.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

public record FindRouteResponse(
        Integer totalDuration,              // 전체 소요 시간 (분)
        LocalDateTime startTime,
        LocalDateTime arriveTime,
        String startPlaceName,
        String arrivePlaceName,
        List<Waypoint> wayPoints,
        List<RouteLeg> legs
) {
    // 경유지
    public record Waypoint(
            String name,
            Double latitude,
            Double longitude,
            Integer order // 순서
    ) {}
    /**
     * 실제 타임라인에 표시되는 단위
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RouteLeg(
            TransportMode mode,              // WALK, BUS, SUBWAY
            Integer sectionTime,              // 구간 소요 시간
            Integer distance,                 // 구간 거리
            String startName,                 // 출발 지점 이름
            String endName,                   // 도착 지점 이름

            // 대중교통 전용
            String routeName,                 // 1024, 303, 2호선 등
            String routeColor,                // 노선 색상
            List<String> stations,            // 경유 정류장 이름 목록
            Integer stationCount,             // 경유 정류장 수

            // 도보 전용
            String walkDescription            // ex "도보 106m (2분)"
    ) {}

    public enum TransportMode {
        WALK, BUS, SUBWAY
    }
}

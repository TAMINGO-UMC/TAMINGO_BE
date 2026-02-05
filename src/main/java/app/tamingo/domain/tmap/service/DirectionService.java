package app.tamingo.domain.tmap.service;

import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.home.dto.Location;
import app.tamingo.domain.tmap.client.TmapTransitClient;
import app.tamingo.domain.tmap.dto.TmapTransitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 경로 계산 서비스
 * TMAP 대중교통 경로 조회 및 우회 여부 판단
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    private final TmapTransitClient tmapTransitClient;

    /**
     * 우회 시간 최대 허용치
     */
    private static final int MAX_DETOUR_MINUTES = 20;

    /**
     * 기본 경로 계산
     * 
     * @param startLat 출발지 위도
     * @param startLng 출발지 경도
     * @param goalLat  목적지 위도
     * @param goalLng  목적지 경도
     * @return DirectionResult
     */
    public DirectionResult calculateRoute(
            double startLat, double startLng,
            double goalLat, double goalLng) {
        return tmapTransitClient.route(startLat, startLng, goalLat, goalLng);
    }

    public TmapTransitResponse calculateRouteDetail(
            double startLat, double startLng,
            double goalLat, double goalLng) {
        return tmapTransitClient.routeResponse(startLat, startLng, goalLat, goalLng);
    }

    /**
     * 할일을 경유지로 포함한 경로 계산
     * 
     * @param startLat 출발지 위도
     * @param startLng 출발지 경도
     * @param todoLat  할일(경유지) 위도
     * @param todoLng  할일(경유지) 경도
     * @param goalLat  목적지 위도
     * @param goalLng  목적지 경도
     * @return DirectionResult
     */
    public DirectionResult calculateRouteWithTodo(
            double startLat, double startLng,
            double todoLat, double todoLng,
            double goalLat, double goalLng) {
        DirectionResult leg1 = tmapTransitClient.route(startLat, startLng, todoLat, todoLng);
        DirectionResult leg2 = tmapTransitClient.route(todoLat, todoLng, goalLat, goalLng);
        if (leg1 == null || leg2 == null) {
            return null;
        }
        int totalMinutes = leg1.getTotalMinutes() + leg2.getTotalMinutes();
        return new DirectionResult(totalMinutes, java.util.List.of());
    }

    /**
     * 여러 경유지를 포함한 경로 계산
     *
     * @param startLat 출발지 위도
     * @param startLng 출발지 경도
     * @param waypoints 경유지 목록 (순서대로)
     * @param goalLat  목적지 위도
     * @param goalLng  목적지 경도
     * @return DirectionResult
     */
    public DirectionResult calculateRouteWithWaypoints(
            double startLat, double startLng,
            List<Location> waypoints,
            double goalLat, double goalLng) {
        List<Location> points = new ArrayList<>();
        points.add(new Location(startLat, startLng));
        if (waypoints != null && !waypoints.isEmpty()) {
            points.addAll(waypoints);
        }
        points.add(new Location(goalLat, goalLng));

        int totalMinutes = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            Location from = points.get(i);
            Location to = points.get(i + 1);
            DirectionResult leg = tmapTransitClient.route(
                    from.latitude(), from.longitude(),
                    to.latitude(), to.longitude()
            );
            if (leg == null) {
                return null;
            }
            totalMinutes += leg.getTotalMinutes();
        }
        return new DirectionResult(totalMinutes, List.of());
    }

    /**
     * 우회 시간이 허용 범위 내인지 판단
     * 
     * @param baseMinutes   기준 경로 소요 시간 (분)
     * @param detourMinutes 우회 경로 소요 시간 (분)
     * @return 허용 범위 내이면 true
     */
    public boolean isDetourAcceptable(int baseMinutes, int detourMinutes) {
        int extraTime = detourMinutes - baseMinutes;
        boolean acceptable = extraTime <= MAX_DETOUR_MINUTES;

        log.debug("[HOME][DETOUR] 우회 시간 계산 base={}min, detour={}min, extra={}min, acceptable={}",
                baseMinutes, detourMinutes, extraTime, acceptable);

        return acceptable;
    }
}

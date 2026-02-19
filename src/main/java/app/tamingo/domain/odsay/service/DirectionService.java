package app.tamingo.domain.odsay.service;

import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.home.dto.Location;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.odsay.client.OdsayTransitClient;
import app.tamingo.domain.odsay.dto.OdsayTransitResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 경로 계산 서비스
 * ODSAY 대중교통 경로 조회 및 우회 여부 판단
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    private final OdsayTransitClient odsayTransitClient;
    private final GeoService geoService;

    /**
     * 우회 시간 최대 허용치
     */
    private static final int MAX_DETOUR_MINUTES = 20;
    private static final int SHORT_DISTANCE_MAX_MINUTES = 10;

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
        double distanceKm = geoService.distanceKm(startLat, startLng, goalLat, goalLng);
        int shortMinutes = geoService.estimateShortDistanceMinutes(distanceKm);
        if (shortMinutes <= SHORT_DISTANCE_MAX_MINUTES) {
            return new DirectionResult(shortMinutes, List.of());
        }
        DirectionResult route = odsayTransitClient.route(startLat, startLng, goalLat, goalLng);
        if (route == null) {
            int fallbackMinutes = geoService.estimateShortDistanceMinutes(distanceKm);
            return new DirectionResult(Math.max(1, fallbackMinutes), List.of());
        }
        return route;
    }

    // 길찾기
    public OdsayTransitResponse calculateRouteDetail(
            double startLat, double startLng,
            double goalLat, double goalLng) {
        double distanceKm = geoService.distanceKm(startLat, startLng, goalLat, goalLng);
        int shortMinutes = geoService.estimateShortDistanceMinutes(distanceKm);
        if (shortMinutes <= SHORT_DISTANCE_MAX_MINUTES) {
            int seconds = shortMinutes * 60;
            int meters = (int) Math.round(distanceKm * 1000.0);
            return shortDistanceResponse(seconds, meters);
        }
        OdsayTransitResponse response = odsayTransitClient.routeResponse(startLat, startLng, goalLat, goalLng);
        if (response == null) {
            int seconds = Math.max(60, shortMinutes) * 60;
            int meters = (int) Math.round(distanceKm * 1000.0);
            return shortDistanceResponse(seconds, meters);
        }
        return response;
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
        DirectionResult leg1 = calculateRoute(startLat, startLng, todoLat, todoLng);
        DirectionResult leg2 = calculateRoute(todoLat, todoLng, goalLat, goalLng);
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
            DirectionResult leg = calculateRoute(
                    from.latitude(), from.longitude(),
                    to.latitude(), to.longitude()
            );
            if (leg == null) {
                double segmentKm = geoService.distanceKm(
                        from.latitude(), from.longitude(),
                        to.latitude(), to.longitude()
                );
                int segmentMinutes = Math.max(1, geoService.estimateShortDistanceMinutes(segmentKm));
                leg = new DirectionResult(segmentMinutes, List.of());
            }
            totalMinutes += leg.getTotalMinutes();
        }
        return new DirectionResult(totalMinutes, List.of());
    }

    private OdsayTransitResponse shortDistanceResponse(int totalSeconds, int totalMeters) {
        OdsayTransitResponse.Leg walkLeg = new OdsayTransitResponse.Leg(
                "WALK",
                totalSeconds,
                totalMeters,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
        OdsayTransitResponse.Itinerary itinerary = new OdsayTransitResponse.Itinerary(
                totalSeconds,
                totalSeconds,
                totalMeters,
                totalMeters,
                List.of(walkLeg)
        );
        return new OdsayTransitResponse(
                new OdsayTransitResponse.MetaData(
                        new OdsayTransitResponse.Plan(List.of(itinerary))
                ),
                "경로가 짧아서 길찾기 응답 제공되지않음"
        );
    }

}

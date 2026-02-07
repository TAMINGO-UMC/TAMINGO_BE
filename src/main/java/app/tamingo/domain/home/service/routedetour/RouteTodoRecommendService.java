package app.tamingo.domain.home.service.routedetour;

import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.home.dto.Location;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.odsay.service.DirectionService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.todo.entity.Todo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 경로 기반 할일 추천 서비스
 * 일정 간 이동 경로를 분석하여 경로 상에 있는 할일을 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteTodoRecommendService {

    private final DirectionService directionService;
    private final GeoService geoService;
    private final RouteBufferFilter routeBufferFilter;

    /**
     * 근처 할일을 찾기 위한 반경
     */
    private static final double NEARBY_RADIUS_KM = 5.0;

    /**
     * 경로 버퍼 거리
     */
    private static final double ROUTE_BUFFER_KM = 0.5;

    public List<RouteDetourCandidate> findRouteDetourCandidates(
            Schedule schedule,
            Schedule nextSchedule,
            List<Todo> allTodos
    ) {
        if (!hasLocation(schedule) || !hasLocation(nextSchedule)) {
            log.debug("[HOME][DETOUR] 일정 장소 없음 scheduleId={}, nextScheduleId={}",
                    schedule.getId(), nextSchedule.getId());
            return List.of();
        }

        List<Todo> nearbyTodos = allTodos.stream()
                .filter(todo -> !todo.isChecked())
                .filter(todo -> todo.getSchedule() == null)
                .filter(this::hasLocation)
                .filter(todo -> isNearSchedules(todo, schedule, nextSchedule))
                .toList();

        if (nearbyTodos.isEmpty()) {
            log.debug("[HOME][DETOUR] 근처에서 할 수 있는 할일이 없음 scheduleId={}, nextScheduleId={}",
                    schedule.getId(), nextSchedule.getId());
            return List.of();
        }

        DirectionResult baseRoute = directionService.calculateRoute(
                schedule.getLatitude(), schedule.getLongitude(),
                nextSchedule.getLatitude(), nextSchedule.getLongitude());

        if (baseRoute == null) {
            log.warn("[HOME][DETOUR] 기준 경로 계산 실패. scheduleId={}, nextScheduleId={}",
                    schedule.getId(), nextSchedule.getId());
            return List.of();
        }

        boolean hasPolyline = baseRoute.getPolyline() != null && !baseRoute.getPolyline().isEmpty();
        RouteDetourCandidate bestCandidate = null;
        double bestDistanceKm = Double.MAX_VALUE;

        for (Todo todo : nearbyTodos) {
            if (hasPolyline && !routeBufferFilter.isNearRoute(
                    todo.getLatitude(), todo.getLongitude(),
                    baseRoute.getPolyline(), ROUTE_BUFFER_KM)) {
                log.debug("Todo not near route. todoId={}, title='{}'", todo.getId(), todo.getTitle());
                continue;
            }

            double distanceKm = hasPolyline
                    ? minDistanceToRouteKm(todo.getLatitude(), todo.getLongitude(), baseRoute.getPolyline())
                    : minDistanceToSchedulesKm(todo, schedule, nextSchedule);

            if (distanceKm < bestDistanceKm) {
                bestDistanceKm = distanceKm;
                bestCandidate = new RouteDetourCandidate(todo, null);
            }
        }

        if (bestCandidate == null) {
            return List.of();
        }

        log.info("[HOME][DETOUR] 우회할 할일 탐색 완료: '{}' (approx distance: {}km)",
                bestCandidate.todo().getTitle(),
                String.format("%.2f", bestDistanceKm));

        return List.of(bestCandidate);
    }

    /**
     * 할일이 일정 근처에 있는지 확인
     */
    private boolean isNearSchedules(Todo todo, Schedule schedule, Schedule nextSchedule) {
        boolean nearStart = geoService.isWithin(
                todo.getLatitude(), todo.getLongitude(),
                schedule.getLatitude(), schedule.getLongitude(),
                NEARBY_RADIUS_KM);

        boolean nearEnd = geoService.isWithin(
                todo.getLatitude(), todo.getLongitude(),
                nextSchedule.getLatitude(), nextSchedule.getLongitude(),
                NEARBY_RADIUS_KM);

        return nearStart || nearEnd;
    }

    /**
     * 일정에 장소 정보가 있는지 확인
     */
    private boolean hasLocation(Schedule schedule) {
        return schedule.getLatitude() != null && schedule.getLongitude() != null;
    }

    /**
     * 할일에 장소 정보가 있는지 확인
     */
    private boolean hasLocation(Todo todo) {
        return todo.getLatitude() != null && todo.getLongitude() != null;
    }

    private double minDistanceToRouteKm(double todoLat, double todoLng, List<Location> polyline) {
        double min = Double.MAX_VALUE;
        for (Location point : polyline) {
            double distance = geoService.distanceKm(
                    todoLat, todoLng,
                    point.latitude(), point.longitude());
            if (distance < min) {
                min = distance;
            }
        }
        return min;
    }

    private double minDistanceToSchedulesKm(Todo todo, Schedule schedule, Schedule nextSchedule) {
        double toStart = geoService.distanceKm(
                todo.getLatitude(), todo.getLongitude(),
                schedule.getLatitude(), schedule.getLongitude());
        double toEnd = geoService.distanceKm(
                todo.getLatitude(), todo.getLongitude(),
                nextSchedule.getLatitude(), nextSchedule.getLongitude());
        return Math.min(toStart, toEnd);
    }

    /**
     *  할일을 스케줄과 연계하여 저장
     */

    public record RouteDetourCandidate(
            Todo todo,
            Integer detourMinutes
    ) {
    }
}

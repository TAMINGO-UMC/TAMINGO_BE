package app.tamingo.domain.home.service.routedetour;

import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionPlanType;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.tmap.service.DirectionService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    /**
     * 일정 간 경로 상에 있는 할일 목록 조회
     * 
     * @param schedule     기준 일정
     * @param nextSchedule 다음 일정
     * @param allTodos     모든 할일 목록
     * @return 경로 상에 있는 할일 목록
     */
    public List<Todo> findRouteTodos(Schedule schedule, Schedule nextSchedule, List<Todo> allTodos) {
        // 1. 일정에 장소 정보가 있는지 확인
        if (!hasLocation(schedule) || !hasLocation(nextSchedule)) {
            log.debug("[HOME][DETOUR] 일정 장소 없음 scheduleId={}, nextScheduleId={}",
                    schedule.getId(), nextSchedule.getId());
            return List.of();
        }

        // 2. 비슷한 장소의 할일 목록 조회 (반경 5km 이내)
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

        // 3. 기준 경로 계산
        DirectionResult baseRoute = directionService.calculateRoute(
                schedule.getLatitude(), schedule.getLongitude(),
                nextSchedule.getLatitude(), nextSchedule.getLongitude());

        if (baseRoute == null) {
            log.warn("[HOME][DETOUR] 기준 경로 계산 실패. scheduleId={}, nextScheduleId={}",
                    schedule.getId(), nextSchedule.getId());
            return List.of();
        }

        boolean hasPolyline = baseRoute.getPolyline() != null && !baseRoute.getPolyline().isEmpty();

        // 4. 각 할일에 대해 우회 경로 계산 및 필터링
        List<Todo> routeTodos = new ArrayList<>();

        for (Todo todo : nearbyTodos) {
            // 경로 버퍼 필터링: 경로 근처에 있는지 확인
            if (hasPolyline && !routeBufferFilter.isNearRoute(
                    todo.getLatitude(), todo.getLongitude(),
                    baseRoute.getPolyline(), ROUTE_BUFFER_KM)) {
                log.debug("Todo not near route. todoId={}, title='{}'", todo.getId(), todo.getTitle());
                continue;
            }

            // 우회 경로 계산
            DirectionResult detourRoute = directionService.calculateRouteWithTodo(
                    schedule.getLatitude(), schedule.getLongitude(),
                    todo.getLatitude(), todo.getLongitude(),
                    nextSchedule.getLatitude(), nextSchedule.getLongitude());

            if (detourRoute == null) {
                log.debug("Failed to calculate detour route. todoId={}", todo.getId());
                continue;
            }

            // 우회 시간이 20분 이하인지 확인, 해당하는 할일만 반환
            if (directionService.isDetourAcceptable(
                    baseRoute.getTotalMinutes(),
                    detourRoute.getTotalMinutes())) {
                routeTodos.add(todo);
                log.info("[HOME][DETOUR] 우회할 할일 탐색 완료: '{}' (base: {}min, detour: {}min, extra: {}min)",
                        todo.getTitle(),
                        baseRoute.getTotalMinutes(),
                        detourRoute.getTotalMinutes(),
                        detourRoute.getTotalMinutes() - baseRoute.getTotalMinutes());
            }
        }

        return routeTodos;
    }

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
        List<RouteDetourCandidate> candidates = new ArrayList<>();

        for (Todo todo : nearbyTodos) {
            if (hasPolyline && !routeBufferFilter.isNearRoute(
                    todo.getLatitude(), todo.getLongitude(),
                    baseRoute.getPolyline(), ROUTE_BUFFER_KM)) {
                log.debug("Todo not near route. todoId={}, title='{}'", todo.getId(), todo.getTitle());
                continue;
            }

            DirectionResult detourRoute = directionService.calculateRouteWithTodo(
                    schedule.getLatitude(), schedule.getLongitude(),
                    todo.getLatitude(), todo.getLongitude(),
                    nextSchedule.getLatitude(), nextSchedule.getLongitude());

            if (detourRoute == null) {
                log.debug("Failed to calculate detour route. todoId={}", todo.getId());
                continue;
            }

            if (directionService.isDetourAcceptable(
                    baseRoute.getTotalMinutes(),
                    detourRoute.getTotalMinutes())) {
                int detourMinutes = detourRoute.getTotalMinutes() - baseRoute.getTotalMinutes();
                candidates.add(new RouteDetourCandidate(todo, detourMinutes));
                log.info("[HOME][DETOUR] 우회할 할일 탐색 완료: '{}' (base: {}min, detour: {}min, extra: {}min)",
                        todo.getTitle(),
                        baseRoute.getTotalMinutes(),
                        detourRoute.getTotalMinutes(),
                        detourMinutes);
            }
        }

        return candidates;
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

    /**
     *  할일을 스케줄과 연계하여 저장
     */

    public record RouteDetourCandidate(
            Todo todo,
            int detourMinutes
    ) {
    }
}

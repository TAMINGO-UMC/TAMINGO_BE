package app.tamingo.domain.home.service.startplace;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;
import app.tamingo.domain.home.dto.Location;
import app.tamingo.domain.home.entity.enums.StartSourceType;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.tmap.service.DirectionService;
import app.tamingo.domain.home.service.startplace.region.ServiceRegionPolicy;
import app.tamingo.domain.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 출발 위치 결정 서비스
 * 일정 출발 1시간 전에 실행되어 출발 위치를 결정
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StartLocationDeciderService {

    private final GeoService geoService;
    private final DirectionService directionService;
    private final ServiceRegionPolicy serviceRegionPolicy;

    private static final double FALLBACK_AVG_SPEED_KMH = 40.0;

    /**
     * 출발 위치 결정
     * 
     * @param currentSchedule  현재 일정
     * @param previousSchedule 이전 일정 / 없으면 null
     * @param gpsLocation      현재 GPS 위치 / 없으면 null
     * @param favoritePlaces   자주 가는 장소 목록
     * @return 결정된 출발 위치
     */
    public Location decideStartLocation(
            Schedule currentSchedule,
            Schedule previousSchedule,
            Location gpsLocation,
            List<FavoritePlace> favoritePlaces) {

        LocationDecision decision = decideStartLocationWithSource(
                currentSchedule,
                previousSchedule,
                gpsLocation,
                favoritePlaces
        );

        return decision != null ? decision.location() : null;
    }

    /**
     * 출발 위치 결정
     */
    public LocationDecision decideStartLocationWithSource(
            Schedule currentSchedule,
            Schedule previousSchedule,
            Location gpsLocation,
            List<FavoritePlace> favoritePlaces) {

        /**
         * 두 일정 사이의 시간 공백이 4시간 미만이면 이전 일정 목적지를 자동 할당
         * 4시간 이상이면 첫 일정 출발지 로직으로 회귀
         */
        if (previousSchedule != null && hasLocation(previousSchedule)
                && timeDiffHours(previousSchedule, currentSchedule) < 4) {
            return new LocationDecision(
                    new Location(previousSchedule.getLatitude(), previousSchedule.getLongitude()),
                    StartSourceType.PREV_SCHEDULE,
                    previousSchedule.getId(),
                    previousSchedule.getPlaceName()
            );
        }

        // 활동 시작 시점의 실시간 GPS가 있으면 즉시 확정
        if (gpsLocation != null) {
            return new LocationDecision(
                    new Location(gpsLocation.latitude(), gpsLocation.longitude()),
                    StartSourceType.GPS,
                    0L,
                    null
            );
        }

        if (favoritePlaces == null || favoritePlaces.isEmpty()) {
            log.warn("[HOME][START] FVP 후보가 없습니다. scheduleId={}", currentSchedule.getId());
            return null;
        }

        List<LocationCandidate> candidates = new ArrayList<>();

        // 자주 가는 장소(FVP) 후보군 수집 - 서비스 지역 내에 존재해야 함
        // TODO : AI 추론 장소는 제외하는 로직 추가 필요
        favoritePlaces.stream()
                .filter(this::hasLocation)
                .filter(fvp -> serviceRegionPolicy.isAllowed(fvp.getLatitude(), fvp.getLongitude()))
                .forEach(fvp -> candidates.add(new LocationCandidate(
                        fvp.getName(),
                        StartSourceType.FVP,
                        fvp.getId(),
                        fvp.getName(),
                        fvp.getLatitude(),
                        fvp.getLongitude())));

        if (candidates.isEmpty()) {
            log.warn("[HOME][START] 서비스 지역 내 FVP 후보가 없습니다. scheduleId={}", currentSchedule.getId());
            return null;
        }

        // FVP 기반 최장 소요 시간 선택
        LocationCandidate longest = candidates.stream()
                .max(Comparator.comparingInt(candidate -> travelMinutesToDestination(
                        candidate.lat, candidate.lng, currentSchedule)))
                .orElse(null);

        if (longest == null) {
            log.warn("[HOME][START] 출발지 결정 실패. scheduleId={}", currentSchedule.getId());
            return null;
        }

        log.info("Start location decided: {} ({}, {}) for schedule '{}'",
                longest.name, longest.lat, longest.lng, currentSchedule.getTitle());
        return new LocationDecision(
                new Location(longest.lat, longest.lng),
                longest.sourceType,
                longest.sourceId,
                longest.placeName
        );
    }

    private boolean hasLocation(Schedule schedule) {
        return schedule.getLatitude() != null && schedule.getLongitude() != null;
    }

    private boolean hasLocation(FavoritePlace place) {
        return place.getLatitude() != null && place.getLongitude() != null;
    }

    // 일정 사이의 시간 차이 계산
    private int timeDiffHours(Schedule earlier, Schedule later) {
        if (earlier == null || later == null) {
            return 0;
        }
        if (earlier.getEndTime() == null || later.getStartTime() == null) {
            return 0;
        }
        return (int) java.time.Duration.between(
                earlier.getEndTime(),
                later.getStartTime()
        ).toHours();
    }

    // 출발지에서 목적지까지의 예상 소요 시간 계산
    private int travelMinutesToDestination(double startLat, double startLng, Schedule schedule) {
        if (schedule == null || schedule.getLatitude() == null || schedule.getLongitude() == null) {
            return 0;
        }
        try {
            return directionService.calculateRoute(
                    startLat, startLng,
                    schedule.getLatitude(), schedule.getLongitude()
            ).getTotalMinutes();
        } catch (RuntimeException e) {
            double distanceKm = geoService.distanceKm(
                    startLat, startLng,
                    schedule.getLatitude(), schedule.getLongitude());
            int fallbackMinutes = (int) Math.round((distanceKm / FALLBACK_AVG_SPEED_KMH) * 60.0);
            log.warn("[HOME][START] 경로 계산 실패로 거리 기반 fallback 사용. scheduleId={}, minutes={}",
                    schedule.getId(), fallbackMinutes, e);
            return fallbackMinutes;
        }
    }

    /**
     * 위치 후보
     */
    private record LocationCandidate(
            String name,
            StartSourceType sourceType,
            Long sourceId,
            String placeName,
            double lat,
            double lng) {
    }

    /**
     * 위치 결정 결과
     */
    public record LocationDecision(
            Location location,
            StartSourceType sourceType,
            Long sourceId,
            String placeName) {
    }
}

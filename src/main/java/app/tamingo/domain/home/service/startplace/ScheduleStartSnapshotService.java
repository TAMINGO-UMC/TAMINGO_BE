package app.tamingo.domain.home.service.startplace;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.home.repository.ScheduleStartSnapshotRepository;
import app.tamingo.domain.kakao.service.KakaoGeoService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.odsay.service.DirectionService;
import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.home.dto.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleStartSnapshotService {

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final ScheduleRepository scheduleRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final ScheduleStartSnapshotRepository scheduleStartSnapshotRepository;
    private final StartLocationDeciderService startLocationDeciderService;
    private final DirectionService directionService;
    private final KakaoGeoService kakaoGeoService;

    /**
     * 해당 일정의 출발지 스냅샷 생성
     * 이전 스케줄이 있다면, 직전 스케줄의 장소를 가져옴
     * @param schedule
     * @param decidedAt
     * @return
     */
    public boolean createSnapshotForSchedule(Schedule schedule, LocalDateTime decidedAt) {
        if (schedule == null) {
            return false;
        }
        if (scheduleStartSnapshotRepository.existsBySchedule(schedule)) {
            return false;
        }

        // 일정에 장소가 없을 경우
        if (schedule.getLatitude() == null || schedule.getLongitude() == null) {
            log.debug("[HOME][START] 일정 장소 누락 scheduleId={}", schedule.getId());
            return false;
        }

        User user = schedule.getUser();

        // 이전 스케줄 불러오기
        Schedule previousSchedule = scheduleRepository
                .findBeforeStartTime(
                        user,
                        schedule.getStartTime()
                )
                .orElse(null);


        // 자주 가는 장소 불러오기
        List<FavoritePlace> favoritePlaces =
                favoritePlaceRepository.findAllByUser(user);

        StartLocationDeciderService.LocationDecision decision =
                startLocationDeciderService.decideStartLocationWithSource(
                        schedule,
                        previousSchedule,
                        null,
                        favoritePlaces
                );

        if (decision == null || decision.location() == null) {
            log.debug("[HOME][START] 출발지 결정 안됨. scheduleId={}", schedule.getId());
            return false;
        }



        // 예상 시간 계산
        int expectedEta = calculateRouteMinutes(
                decision.location().latitude(),
                decision.location().longitude(),
                schedule.getLatitude(),
                schedule.getLongitude()
        );

        ScheduleStartSnapshot snapshot = ScheduleStartSnapshot.of(
                schedule,
                decision.sourceType(),
                decision.sourceId(),
                decision.location().latitude(),
                decision.location().longitude(),
                decision.placeName(),
                decidedAt,
                expectedEta,
                false
        );

        scheduleStartSnapshotRepository.save(snapshot);
        return true;
    }

    // 일정 변경 시 스냅샷 갱신
    public void refreshSnapshotForSchedule(Schedule schedule, LocalDateTime decidedAt) {
        if (schedule == null) {
            return;
        }
        scheduleStartSnapshotRepository.findBySchedule(schedule)
                .ifPresent(scheduleStartSnapshotRepository::delete);
        createSnapshotForSchedule(schedule, decidedAt);
    }

    // 길찾기 비활성화 등으로 스냅샷 제거
    public void deleteSnapshotForSchedule(Schedule schedule) {
        if (schedule == null) {
            return;
        }
        scheduleStartSnapshotRepository.findBySchedule(schedule)
                .ifPresent(scheduleStartSnapshotRepository::delete);
    }

    // SILENT GPS CHECK 적용 - 출발지 스냅샷이 존재하고, GPS 위치가 스냅샷보다 더 오래 걸리는 경우에만 오버라이드
    public StartGpsUpdateResult applySilentGpsCheck(
            Long userId,
            Long scheduleId,
            Location gpsLocation
    ) {
        Schedule schedule = getScheduleForUser(userId, scheduleId);
        if (!hasLocation(schedule) || gpsLocation == null) {
            return StartGpsUpdateResult.notApplied("MISSING_LOCATION");
        }

        ScheduleStartSnapshot snapshot = scheduleStartSnapshotRepository
                .findBySchedule(schedule)
                .orElse(null);

        // 스냅샷이 없으면 적용 불가
        if (snapshot == null) {
            return StartGpsUpdateResult.notApplied("SNAPSHOT_NOT_FOUND");
        }

        // 이미 오버라이드됐을 경우 적용 불가
        if (snapshot.isOverridden()) {
            return StartGpsUpdateResult.notApplied("ALREADY_OVERRIDDEN");
        }

        // 스냅샷 경로 시간과 현재 받아온 GPS 경로 시간 각각 계산
        int snapshotMinutes = calculateRouteMinutes(
                snapshot.getUsedStartLat(),
                snapshot.getUsedStartLng(),
                schedule.getLatitude(),
                schedule.getLongitude()
        );
        int gpsMinutes = calculateRouteMinutes(
                gpsLocation.latitude(),
                gpsLocation.longitude(),
                schedule.getLatitude(),
                schedule.getLongitude()
        );

        // GPS 경로 시간이 더 오래 걸리면 오버라이드
        if (gpsMinutes > snapshotMinutes) {
            // 위경도로 출발지명 검색
            String placeName = findPlaceNameWithLocation(
                    gpsLocation.latitude(),
                    gpsLocation.longitude()
            );

            // 스냅샷 오버라이드 처리
            snapshot.overrideWithGps(
                    gpsLocation.latitude(),
                    gpsLocation.longitude(),
                    placeName,
                    LocalDateTime.now(TARGET_ZONE),
                    gpsMinutes

            );

            scheduleStartSnapshotRepository.save(snapshot);
            return StartGpsUpdateResult.overridden(snapshotMinutes, gpsMinutes,
                    snapshot.getUsedStartLat(), snapshot.getUsedStartLng());
        }

        return StartGpsUpdateResult.notApplied("NOT_LONGER", snapshotMinutes, gpsMinutes,
                snapshot.getUsedStartLat(), snapshot.getUsedStartLng());
    }


    // 사용자와 일정 검증 및 일정 조회
    private Schedule getScheduleForUser(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        if (schedule.getUser() == null || !schedule.getUser().getId().equals(userId)) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }
        return schedule;
    }

    private boolean hasLocation(Schedule schedule) {
        return schedule.getLatitude() != null && schedule.getLongitude() != null;
    }

    private int calculateRouteMinutes(
            double startLat,
            double startLng,
            double goalLat,
            double goalLng
    ) {
        DirectionResult result = directionService.calculateRoute(startLat, startLng, goalLat, goalLng);
        return result != null ? result.getTotalMinutes() : 0;
    }

    // 출발지 GPS 업데이트 결과
    public record StartGpsUpdateResult(
            boolean overridden,
            String reason,
            Integer snapshotMinutes,
            Integer gpsMinutes,
            Double usedStartLat,
            Double usedStartLng
    ) {
        public static StartGpsUpdateResult notApplied(String reason) {
            return new StartGpsUpdateResult(false, reason, null, null, null, null);
        }

        public static StartGpsUpdateResult notApplied(
                String reason,
                int snapshotMinutes,
                int gpsMinutes,
                double usedStartLat,
                double usedStartLng
        ) {
            return new StartGpsUpdateResult(false, reason, snapshotMinutes, gpsMinutes, usedStartLat, usedStartLng);
        }

        public static StartGpsUpdateResult overridden(
                int snapshotMinutes,
                int gpsMinutes,
                double usedStartLat,
                double usedStartLng
        ) {
            return new StartGpsUpdateResult(true, "OVERRIDDEN",
                    snapshotMinutes, gpsMinutes, usedStartLat, usedStartLng);
        }

        public static StartGpsUpdateResult confirmed(double usedStartLat, double usedStartLng) {
            return new StartGpsUpdateResult(false, "CONFIRMED",
                    null, null, usedStartLat, usedStartLng);
        }
    }

    public String findPlaceNameWithLocation(double latitude, double longitude) {
        return kakaoGeoService.getAddress(latitude, longitude).addressName();
    }

    @Transactional(readOnly = true)
    public StartLocationSnapshotInfo findSnapshotLocation(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        ScheduleStartSnapshot snapshot = scheduleStartSnapshotRepository
                .findBySchedule(schedule)
                .orElse(null);
        if (snapshot == null) {
            return null;
        }
        return new StartLocationSnapshotInfo(
                snapshot.getUsedStartLat(),
                snapshot.getUsedStartLng(),
                snapshot.getUsedStartPlaceName()
        );
    }

    @Transactional(readOnly = true)
    public ScheduleStartSnapshot findSnapshotEntity(Schedule schedule) {
        if (schedule == null) {
            return null;
        }
        return scheduleStartSnapshotRepository
                .findBySchedule(schedule)
                .orElse(null);
    }

    public void saveSnapshot(ScheduleStartSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }
        scheduleStartSnapshotRepository.save(snapshot);
    }

    public record StartLocationSnapshotInfo(
            double usedStartLat,
            double usedStartLng,
            String usedStartPlaceName
    ) {
    }
}

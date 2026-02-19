package app.tamingo.domain.home.service.realtime;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.time.VirtualTimeService;
import app.tamingo.domain.home.converter.DailyScheduleResponseConverter;
import app.tamingo.domain.home.converter.FindRouteResponseConverter;
import app.tamingo.domain.home.dto.*;
import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.home.entity.enums.CurrentStatus;
import app.tamingo.domain.home.entity.enums.TimeSlot;
import app.tamingo.domain.home.exception.HomeErrorCode;
import app.tamingo.domain.home.redis.RealtimeActiveSchedule;
import app.tamingo.domain.home.redis.RealtimeSchedule;
import app.tamingo.domain.home.redis.RealtimeScheduleArrivalCheck;
import app.tamingo.domain.home.entity.enums.ArrivedStatus;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.home.service.startplace.ScheduleStartSnapshotService;
import app.tamingo.domain.notification.dto.NotificationMessage;
import app.tamingo.domain.notification.enums.NotificationType;
import app.tamingo.domain.notification.service.NotificationProducer;
import app.tamingo.domain.notificationsetting.entity.AlertMinute;
import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.odsay.dto.OdsayTransitResponse;
import app.tamingo.domain.odsay.exception.OdsayErrorCode;
import app.tamingo.domain.odsay.service.DirectionService;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.entity.DepartureAlarm;
import app.tamingo.domain.userlearning.entity.UserLearningPattern;
import app.tamingo.domain.userlearning.repository.DepartureAlarmRepository;
import app.tamingo.domain.userlearning.repository.UserLearningPatternRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static io.lettuce.core.pubsub.PubSubOutput.Type.message;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimeScheduleService {

    // Redis TTL 설정
    private static final int REALTIME_SCHEDULE_TTL_AFTER_END_MIN = 30;
    private static final int FALLBACK_PUSH_AFTER_MIN = 30;
    private static final int ARRIVAL_CHECK_TTL_AFTER_END_MIN = 1440;
    private static final int NO_DATA_FINALIZE_AFTER_END_MIN = 30;
    private static final int ACTIVE_SCHEDULE_PRE_START_MIN = 10;
    private static final int MIN_PATTERN_SAMPLES = 5;
    private static final int MAX_ETA_ADJUST_MIN = 20;
    private static final double USF_ALPHA = 0.3;
    private static final double MIN_LOCATION_CHANGE_KM = 0.03;

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ScheduleRepository scheduleRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final DirectionService directionService;
    private final GeoService geoService;
    private final RealtimeScheduleRedisService realtimeScheduleRedisService;
    private final FindRouteResponseConverter findRouteResponseConverter;
    private final DepartureAlarmRepository departureAlarmRepository;
    private final UserRepository userRepository;
    private final ScheduleStartSnapshotService scheduleStartSnapshotService;
    private final TodoRepository todoRepository;
    private final UserLearningPatternRepository userLearningPatternRepository;
    private final UserMobilityLearningService userMobilityLearningService;
    private final NotificationProducer notificationProducer;
    private final VirtualTimeService virtualTimeService;

    // 사용자 출발 처리, 길찾기 시작
    @Transactional
    public FindRouteResponse updateDepartureStatus(Long userId, StartLocationGpsRequest request) {
        Long scheduleId = request.scheduleId();
        Double startLat = request.latitude();
        Double startLng = request.longitude();

        Schedule schedule = findScheduleById(scheduleId);
        // 소유자 확인
        ensureOwner(schedule, userId);

        // 목적지 정보 없으면 길찾기 불가, 네비게이션 비활성화 시 길찾기 불가
        if (!hasDestination(schedule) || !Boolean.TRUE.equals(schedule.getIsNavigationEnabled())) {
            return null;
        }

        LocalDateTime now = virtualTimeService.now();

        // status 조회 및 처리
        // 도착 처리된 일정 -> 조회 불가 및 예외처리
        // 출발 처리된 일정 -> 길찾기 정보만 조회
        RealtimeSchedule status = realtimeScheduleRedisService.findScheduleStatus(schedule.getId());
        if (status != null && status.getActualArrivalTime() != null) {
            throw new CustomException(HomeErrorCode.ALREADY_ARRIVED);
        }
        boolean alreadyDeparted = status != null && status.getActualDepartureTime() != null;

        // 연결된 할일을 조회, 위치정보 있는 할일만 조회
        List<Todo> todos = todoRepository.findAllByScheduleAndLocation(schedule);

        // 경유지 개수를 100개 미만으로 제한
        if (todos.size() > 100 )  {
            throw new CustomException(HomeErrorCode.TOO_MANY_TODOS);
        }
        List<Location> wayPointLocations = toWaypointLocations(todos);
        List<FindRouteResponse.Waypoint> wayPoints = toWaypoints(todos);

        List<FindRouteResponse.RouteLeg> legs = new ArrayList<>();
        Integer totalTimeSeconds = null;

        // 경유지 분기, 길찾기 응답 생성
        if (wayPointLocations.isEmpty()) {
            OdsayTransitResponse routeResponse = directionService.calculateRouteDetail(
                    startLat,
                    startLng,
                    schedule.getLatitude(),
                    schedule.getLongitude()
            );
            OdsayTransitResponse.Itinerary itinerary = firstItinerary(routeResponse);
            if (itinerary == null || itinerary.totalTime() == null) {
                return null;
            }
            totalTimeSeconds = itinerary.totalTime();
            legs.addAll(findRouteResponseConverter.toRouteLegs(itinerary));
        } else {
            List<Location> points = new ArrayList<>();
            points.add(new Location(startLat, startLng));
            points.addAll(wayPointLocations);
            points.add(new Location(schedule.getLatitude(), schedule.getLongitude()));

            int totalSeconds = 0;
            for (int i = 0; i < points.size() - 1; i++) {
                Location from = points.get(i);
                Location to = points.get(i + 1);
                OdsayTransitResponse segmentResponse = directionService.calculateRouteDetail(
                        from.latitude(),
                        from.longitude(),
                        to.latitude(),
                        to.longitude()
                );
                OdsayTransitResponse.Itinerary segment = firstItinerary(segmentResponse);
                if (segment == null || segment.totalTime() == null) {
                    return null;
                }
                totalSeconds += segment.totalTime();
                legs.addAll(findRouteResponseConverter.toRouteLegs(segment));
            }
            totalTimeSeconds = totalSeconds;
        }

        // ETA 계산 및 저장
        int etaMinutes = findRouteResponseConverter.toMinutes(totalTimeSeconds);
        etaMinutes = applyUserPattern(schedule.getUser(), schedule.getStartTime(), etaMinutes);
        int arrivalBufferMinutes = getArrivalBufferMinutes(schedule);

        // 출발지 정보 가져오기
        String placeName = scheduleStartSnapshotService.findPlaceNameWithLocation(request.latitude(), request.longitude());

        if (alreadyDeparted) {
            String arrivePlaceName = schedule.getPlaceName() != null ? schedule.getPlaceName() : "DESTINATION";
            LocalDateTime arriveTime = now.plusMinutes(etaMinutes);
            return new FindRouteResponse(
                    etaMinutes,
                    now,
                    arriveTime,
                    placeName,
                    arrivePlaceName,
                    wayPoints,
                    legs
            );
        }

        // 예상 출발/도착 시간 계산
        ExpectedTimes expectedTimes = computeExpectedTimes(schedule, arrivalBufferMinutes, etaMinutes);

        // 사용자 이동 오차 로그 초기 저장
        userMobilityLearningService.saveInitialErrorLogIfFirst(schedule, placeName, etaMinutes);

        saveEta(schedule, etaMinutes, arrivalBufferMinutes, expectedTimes, now);

        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), REALTIME_SCHEDULE_TTL_AFTER_END_MIN);
        LocalDateTime etaArrival = now.plusMinutes(etaMinutes);
        long diffMinutes = Duration.between(etaArrival, schedule.getStartTime()).toMinutes();
        CurrentStatus currentStatus = resolveStatus(diffMinutes);
        int leftOrDelayMinutes = (int) Math.abs(diffMinutes);
        Integer lateArrivalMinutes = diffMinutes < 0 ? (int) Math.abs(diffMinutes) : null;

        RealtimeSchedule realtimeSchedule = realtimeScheduleRedisService
                .getOrCreateScheduleStatus(schedule.getId(), now.format(ISO), ttlSec);
        applyNavigationEnabled(realtimeSchedule, schedule);
        realtimeSchedule.applyStatus(
                currentStatus,
                true,
                leftOrDelayMinutes,
                lateArrivalMinutes,
                expectedTimes.departure.format(ISO),
                expectedTimes.arrival.format(ISO),
                now.format(ISO),
                ttlSec
        );
        realtimeSchedule.updateNavigationEnabled(true);
        realtimeScheduleRedisService.saveScheduleStatus(realtimeSchedule);
        saveActiveSchedule(schedule, now, ttlSec);
        markActualDeparture(
                schedule,
                expectedTimes,
                currentStatus,
                leftOrDelayMinutes,
                lateArrivalMinutes,
                now,
                ttlSec
        );

        // 길찾기 응답 생성
        String startPlaceName = scheduleStartSnapshotService.findPlaceNameWithLocation(request.latitude(),request.longitude());
        String arrivePlaceName = schedule.getPlaceName() != null ? schedule.getPlaceName() : "DESTINATION";
        LocalDateTime arriveTime = now.plusMinutes(etaMinutes);

        return new FindRouteResponse(
                etaMinutes,
                now,
                arriveTime,
                startPlaceName,
                arrivePlaceName,
                wayPoints,
                legs
        );
    }

    // 실시간 사용자 위치 기반 ETA 업데이트
    @Transactional
    public UserGpsResponse updateRealtime(Long userId, Long scheduleId, double userLat, double userLng) {
        LocalDateTime now = virtualTimeService.now();
        Schedule schedule = findScheduleById(scheduleId);
        RealtimeSchedule previousStatus = realtimeScheduleRedisService.findScheduleStatus(schedule.getId());
        saveLocation(schedule, userLat, userLng, now);

        if (!hasDestination(schedule)) {
            throw new CustomException(HomeErrorCode.SCHEDULE_NAVIGATION_DISABLED);
        }

        boolean isArrived = isArrived(schedule, userLat, userLng, 0.05);

        // 도착 처리
        if (isLocationChangeSmall(previousStatus, userLat, userLng)) {
            handleArrivalIfNeeded(schedule, userLat, userLng, now, "CURRENT_LOCATION");
            return new UserGpsResponse(isArrived);
        }

        DirectionResult route = directionService.calculateRoute(
                userLat,
                userLng,
                schedule.getLatitude(),
                schedule.getLongitude()
        );
        if (route == null) {
            throw new CustomException(OdsayErrorCode.REQUEST_FAILED);
        }

        int arrivalBufferMinutes = getArrivalBufferMinutes(schedule);

        int etaMinutes = route.getTotalMinutes();
        etaMinutes = applyUserPattern(schedule.getUser(), schedule.getStartTime(), etaMinutes);
        checkTrafficAndNotify(schedule, etaMinutes);
        ExpectedTimes expectedTimes = computeExpectedTimes(schedule, arrivalBufferMinutes, etaMinutes);

        saveEta(schedule, etaMinutes, arrivalBufferMinutes, expectedTimes, now);
        saveStatus(schedule, expectedTimes, now);

        handleArrivalIfNeeded(schedule, userLat, userLng, now, "CURRENT_LOCATION");
        return new UserGpsResponse(isArrived);
    }

    private boolean isLocationChangeSmall(RealtimeSchedule previousStatus, double userLat, double userLng) {
        if (previousStatus == null
                || previousStatus.getLatitude() == null
                || previousStatus.getLongitude() == null) {
            return false;
        }
        return geoService.isWithin(
                previousStatus.getLatitude(),
                previousStatus.getLongitude(),
                userLat,
                userLng,
                MIN_LOCATION_CHANGE_KM
        );
    }

    // 출발로 처리
    private void markActualDeparture(
            Schedule schedule,
            ExpectedTimes expectedTimes,
            CurrentStatus currentStatus,
            int leftOrDelayMinutes,
            Integer lateArrivalMinutes,
            LocalDateTime now,
            long ttlSec
    ) {
        String key = RealtimeSchedule.key(schedule.getId());
        RealtimeSchedule realtime = realtimeScheduleRedisService
                .getOrCreateScheduleStatus(schedule.getId(), now.format(ISO), ttlSec);
        if (realtime.getActualDepartureTime() != null) {
            throw new CustomException(HomeErrorCode.ALREADY_DEPARTED);
        }
        realtime.applyStatus(
                currentStatus,
                true,
                leftOrDelayMinutes,
                lateArrivalMinutes,
                expectedTimes.departure.format(ISO),
                expectedTimes.arrival.format(ISO),
                now.format(ISO),
                ttlSec
        );
        applyNavigationEnabled(realtime, schedule);
        realtime.updateActualDepartureTime(now.format(ISO));
        realtimeScheduleRedisService.saveScheduleStatus(realtime);
    }

    // 실시간 일정 상태 조회, Redis 생성 전이면 기본 설정 - startSnapshot에서 가져오기
    @Transactional(readOnly = true)
    public DailyScheduleResponse.ScheduleStatusResponse calculateScheduleStatus(Schedule schedule) {
        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(schedule.getId());
        if (realtime != null) {
            return realtimeScheduleRedisService.toStatusResponse(realtime);
        }
        else {
            return resolveStatusByNow(virtualTimeService.now(), schedule);
        }
    }

    private DailyScheduleResponse.ScheduleStatusResponse resolveStatusByNow(LocalDateTime now, Schedule schedule) {
        ScheduleStartSnapshot snapshot =
                scheduleStartSnapshotService.findSnapshotEntity(schedule);
        LocalDateTime expectedDeparture = schedule.getStartTime();
        LocalDateTime expectedArrival = schedule.getStartTime();
        if (snapshot != null) {
            if (snapshot.getDepartureTime() != null) {
                expectedDeparture = snapshot.getDepartureTime();
            }
            if (snapshot.getArrivalTime() != null) {
                expectedArrival = snapshot.getArrivalTime();
            }
        }

        // 현재시간, 출발예상시간 차이 가져오기
        long diffMinutes = Duration.between(now, expectedDeparture).toMinutes();
        CurrentStatus status = resolveStatus(diffMinutes);
        int leftOrDelayMinutes = (int) Math.abs(diffMinutes);
        long arrivalDiffMinutes = Duration.between(now, expectedArrival).toMinutes();
        Integer lateArrivalMinutes = arrivalDiffMinutes < 0 ? (int) Math.abs(arrivalDiffMinutes) : null;
        boolean isStarted = !expectedDeparture.isAfter(now);

        return new DailyScheduleResponse.ScheduleStatusResponse(
                status,
                isStarted,
                leftOrDelayMinutes,
                expectedDeparture.toLocalTime(),
                expectedArrival.toLocalTime(),
                lateArrivalMinutes
        );
    }


    // 현재 시간, 사용자 위치 기반으로 예상 도착 시간(ETA) 및 상태 저장 ->  Redis
    private void saveLocation(Schedule schedule, double userLat, double userLng, LocalDateTime now) {
        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), REALTIME_SCHEDULE_TTL_AFTER_END_MIN);
        RealtimeSchedule realtime = realtimeScheduleRedisService
                .getOrCreateScheduleStatus(schedule.getId(), now.format(ISO), ttlSec);
        applyNavigationEnabled(realtime, schedule);
        realtime.applyLocation(userLat, userLng, now.format(ISO), ttlSec);
        realtimeScheduleRedisService.saveScheduleStatus(realtime);
    }

    // ETA 저장
    private void saveEta(Schedule schedule, int etaMinutes, int arrivalBufferMinutes, ExpectedTimes expectedTimes, LocalDateTime now) {
        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), REALTIME_SCHEDULE_TTL_AFTER_END_MIN);
        RealtimeSchedule realtime = realtimeScheduleRedisService
                .getOrCreateScheduleStatus(schedule.getId(), now.format(ISO), ttlSec);
        applyNavigationEnabled(realtime, schedule);
        realtime.applyEta(
                etaMinutes,
                arrivalBufferMinutes,
                expectedTimes.departure.format(ISO),
                expectedTimes.arrival.format(ISO),
                now.format(ISO),
                ttlSec
        );
        realtimeScheduleRedisService.saveScheduleStatus(realtime);
    }

    // 상태 저장 - 예상 시작,도착 시간
    private void saveStatus(Schedule schedule, ExpectedTimes expectedTimes, LocalDateTime now) {
        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), REALTIME_SCHEDULE_TTL_AFTER_END_MIN);

        // 예상 소요시간
        long diffMinutes = Duration.between(expectedTimes.arrival, schedule.getStartTime()).toMinutes();
        CurrentStatus status = resolveStatus(diffMinutes);
        int leftOrDelayMinutes = (int) Math.abs(diffMinutes);
        Integer lateArrivalMinutes = diffMinutes < 0 ? (int) Math.abs(diffMinutes) : null;

        boolean isStarted = !expectedTimes.departure.isAfter(now);

        RealtimeSchedule realtime = realtimeScheduleRedisService
                .getOrCreateScheduleStatus(schedule.getId(), now.format(ISO), ttlSec);
        applyNavigationEnabled(realtime, schedule);
        realtime.applyStatus(
                status,
                isStarted,
                leftOrDelayMinutes,
                lateArrivalMinutes,
                expectedTimes.departure.format(ISO),
                expectedTimes.arrival.format(ISO),
                now.format(ISO),
                ttlSec
        );
        realtimeScheduleRedisService.saveScheduleStatus(realtime);
    }

    // 지각/출발 상태 판별
    private CurrentStatus resolveStatus(long diffMinutes) {
        if (diffMinutes >= 20) {
            return CurrentStatus.READY;
        }
        if (diffMinutes > 0) {
            return CurrentStatus.DEPARTED;
        }
        if (diffMinutes == 0) {
            return CurrentStatus.DEPARTURE_DELAYED;
        }
        return CurrentStatus.DEPARTURE_EXTREME_DELAYED;
    }

    // 사용자가 설정한 도착 알림 버퍼 시간 조회
    private int getArrivalBufferMinutes(Schedule schedule) {
        NotificationSetting setting = notificationSettingRepository
                .findById(schedule.getUser().getId())
                .orElse(null);
        if (setting == null || !setting.isDepartureAlertEnabled()) {
            return AlertMinute.MIN_10.getMinutes();
        }
        return setting.getDepartureLeadMinutes();
    }

    // TTL 계산
    private long ttlSecondsUntil(LocalDateTime baseTime, int plusMinutes) {
        LocalDateTime now = virtualTimeService.now();
        LocalDateTime expireAt = baseTime.plusMinutes(plusMinutes);
        long ttlSec = Duration.between(now, expireAt).getSeconds();
        return ttlSec > 0 ? ttlSec : 1;
    }


    private boolean isArrived(Schedule schedule, double userLat, double userLng, double km) {
        if (schedule.getLatitude() == null || schedule.getLongitude() == null) {
            return false;
        }
        return geoService.isWithin(
                userLat, userLng,
                schedule.getLatitude(), schedule.getLongitude(),
                km
        );
    }

    private boolean isLateByMinutes(LocalDateTime startTime, LocalDateTime now, int minutes) {
        long diff = Duration.between(startTime, now).toMinutes();
        return diff == minutes;
    }

    // 도착 시간 기록
    @Transactional
    public void markActualArrivalIfFirst(Long scheduleId, LocalDateTime now) {
        Schedule schedule = findScheduleById(scheduleId);
        recordActualArrivalAndFinalize("CURRENT_LOCATION", schedule, now);
    }

    // 주간 리포트에 pscore 기록
    @Transactional
    public void recordPScoreIfFirst(Schedule schedule, int score, String source, LocalDateTime now) {
        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), ARRIVAL_CHECK_TTL_AFTER_END_MIN);
        RealtimeScheduleArrivalCheck check = realtimeScheduleRedisService
                .getOrCreateArrivalCheck(schedule.getId(), ttlSec);

        if (check.getPScore() != null) {
            return;
        }

        check.applyPScore(score, source, now.format(ISO), ttlSec);
        realtimeScheduleRedisService.saveArrivalCheck(check);
        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(schedule.getId());
        boolean navigationUsed = realtime != null && Boolean.TRUE.equals(realtime.getNavigationEnabled());
        userMobilityLearningService.saveScheduleResultIfFirst(
                schedule,
                score,
                resolveArrivedStatusFromRedis(schedule.getId()),
                now,
                navigationUsed
        );
    }

    @Transactional
    public void recordPScoreForArrival(Schedule schedule, LocalDateTime arrivedAt, String source, boolean isPostConfirm) {
        if (schedule == null || arrivedAt == null) {
            return;
        }
        int score = computePScore(schedule.getStartTime(), arrivedAt, isPostConfirm);
        recordPScoreIfFirst(schedule, score, source, arrivedAt);
    }


    // 도착 확인 푸시 발송 및 Redis 기록
    @Transactional
    public void triggerArrivalCheckIfNeeded(Schedule schedule, LocalDateTime now) {
        LocalDateTime triggerAt = schedule.getStartTime().plusMinutes(FALLBACK_PUSH_AFTER_MIN);
        if (now.isBefore(triggerAt)) {
            return;
        }
        if (hasActualArrival(schedule.getId())) {
            return;
        }

        RealtimeScheduleArrivalCheck check = realtimeScheduleRedisService.findArrivalCheck(schedule.getId());
        if (check != null && check.getFallbackPushSentAt() != null) {
            return;
        }

        // 도착 확인 푸시 발송
        // TODO : "도착하셨나요?" 푸시 발송 로직 연결
        sendArrivalConfirmationPush(schedule);

        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), ARRIVAL_CHECK_TTL_AFTER_END_MIN);
        if (check == null) {
            check = RealtimeScheduleArrivalCheck.create(schedule.getId(), ttlSec);
        }
        check.markFallbackPushSent(now.format(ISO), ttlSec);
        realtimeScheduleRedisService.saveArrivalCheck(check);
    }


    // 사후 확인 처리
    @Transactional
    public void confirmArrivalByPostCheck(Long userId, Long scheduleId) {
        User user = userRepository.getReferenceById(userId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }
        LocalDateTime now = virtualTimeService.now();

        RealtimeScheduleArrivalCheck check = realtimeScheduleRedisService.findArrivalCheck(scheduleId);
        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), ARRIVAL_CHECK_TTL_AFTER_END_MIN);
        if (check == null) {
            check = RealtimeScheduleArrivalCheck.create(scheduleId, ttlSec);
        }

        if (!Boolean.TRUE.equals(check.getArrivedConfirmed())) {
            check.confirmArrival(now.format(ISO), ttlSec);
            realtimeScheduleRedisService.saveArrivalCheck(check);
        }

        recordPScoreForArrival(schedule, now, "POST_CONFIRM", true);
        recordActualArrivalAndFinalize("CURRENT_LOCATION", schedule, now);
    }

    // gps 추적 안됨, 사후 처리 응답 없을 경우 점수처리
    @Transactional
    public void finalizeNoDataIfNeeded(Schedule schedule, LocalDateTime now) {
        LocalDateTime finalizeAt = resolveEndTime(schedule).plusMinutes(NO_DATA_FINALIZE_AFTER_END_MIN);
        if (now.isBefore(finalizeAt)) {
            return;
        }
        if (hasActualArrival(schedule.getId())) {
            return;
        }

        RealtimeScheduleArrivalCheck check = realtimeScheduleRedisService.findArrivalCheck(schedule.getId());
        if (check == null || check.getFallbackPushSentAt() == null) {
            return;
        }
        if (Boolean.TRUE.equals(check.getArrivedConfirmed())) {
            return;
        }
        if (check.getPScore() != null) {
            return;
        }

        recordPScoreIfFirst(schedule, 0, "NO_DATA", now);
    }

    // 실제 도착했는지 조회
    private boolean hasActualArrival(Long scheduleId) {
        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(scheduleId);
        return realtime != null && realtime.getActualArrivalTime() != null;
    }

    // 실제 도착 시간 기록 및 오차 로그 저장
    private void recordActualArrivalAndFinalize(String departurePlace, Schedule schedule, LocalDateTime now) {
        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(schedule.getId());
        if (realtime == null) {
            return;
        }
        if (realtime.getActualArrivalTime() != null) {
            return;
        }

        realtime.updateActualArrivalTime(now.format(ISO));
        realtimeScheduleRedisService.saveScheduleStatus(realtime);
        clearActiveSchedule(schedule);

        recordPScoreForArrival(schedule, now, "ARRIVAL", false);

        Integer etaMinutes = realtime.getEtaMinutes();
        var snapshot = scheduleStartSnapshotService.findSnapshotEntity(schedule);
        if (etaMinutes == null && snapshot != null) {
            etaMinutes = snapshot.getExpectedEta();
        }

        LocalDateTime actualDeparture = parseLocalDateTime(realtime.getActualDepartureTime());
        if (actualDeparture == null) {
            actualDeparture = parseLocalDateTime(realtime.getExpectedDepartureTime());
        }
        if (actualDeparture == null && snapshot != null) {
            actualDeparture = snapshot.getDepartureTime();
        }
        if (actualDeparture == null && etaMinutes != null) {
            actualDeparture = now.minusMinutes(etaMinutes);
        }
        if (actualDeparture == null) {
            int arrivalBufferMinutes = realtime.getArrivalBufferMinutes() != null
                    ? realtime.getArrivalBufferMinutes()
                    : getArrivalBufferMinutes(schedule);
            LocalDateTime expectedArrival = schedule.getStartTime().minusMinutes(arrivalBufferMinutes);
            actualDeparture = expectedArrival != null ? expectedArrival.minusMinutes(0) : null;
        }
        if (actualDeparture == null) {
            actualDeparture = now;
        }

        int realDuration = (int) Duration.between(actualDeparture, now).toMinutes();
        if (realDuration < 0) {
            realDuration = 0;
        }
        ArrivedStatus arrivedStatus = resolveArrivedStatusFromRedis(schedule.getId());

        // 최종 에러 로그 저장
        userMobilityLearningService.saveFinalErrorLog(
                departurePlace,
                schedule,
                etaMinutes != null ? etaMinutes : realDuration,
                realDuration,
                arrivedStatus
        );

        updateDepartureAlarmOnArrival(schedule, realtime,
                etaMinutes != null ? etaMinutes : realDuration,
                realDuration,
                arrivedStatus);
    }

    private void saveActiveSchedule(Schedule schedule, LocalDateTime now, long ttlSec) {
        if (schedule == null) {
            return;
        }
        RealtimeActiveSchedule active = realtimeScheduleRedisService.getOrCreateActiveSchedule(
                schedule.getUser().getId(),
                schedule.getId(),
                now.format(ISO),
                ttlSec
        );
        realtimeScheduleRedisService.saveActiveSchedule(active);
    }

    private void clearActiveSchedule(Schedule schedule) {
        if (schedule == null) {
            return;
        }
        realtimeScheduleRedisService.deleteActiveSchedule(schedule.getUser().getId());
    }

    // Redis에서 Status 파싱
    private ArrivedStatus resolveArrivedStatusFromRedis(Long scheduleId) {
        String key = RealtimeSchedule.key(scheduleId);
        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(scheduleId);
        if (realtime == null) {
            return ArrivedStatus.ON_TIME;
        }
        LocalDateTime expectedArrival = parseLocalDateTime(realtime.getExpectedArrivalTime());
        LocalDateTime actualArrival = parseLocalDateTime(realtime.getActualArrivalTime());
        if (expectedArrival == null || actualArrival == null) {
            return ArrivedStatus.ON_TIME;
        }
        long diffMinutes = Duration.between(expectedArrival, actualArrival).toMinutes();
        if (diffMinutes > 0) {
            return ArrivedStatus.LATE;
        }
        if (diffMinutes < 0) {
            return ArrivedStatus.EARLY;
        }
        return ArrivedStatus.ON_TIME;
    }

    private LocalDateTime parseLocalDateTime(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDateTime.parse(dateTime, ISO);
    }

    // 일정 종료 시간 전까지 TTL로 설정
    private LocalDateTime resolveEndTime(Schedule schedule) {
        return schedule.getEndTime() != null ? schedule.getEndTime() : schedule.getStartTime();
    }

    private void sendArrivalConfirmationPush(Schedule schedule) {
        User user = schedule.getUser();

        notificationProducer.reserve(
                NotificationMessage.createArrival(
                        user.getId(),
                        user.getNickname(),
                        schedule.getPlaceName(),
                        0
                ),
                virtualTimeService.now()
        );

        log.info("[6번 도착 확인] {}님 예약", user.getNickname());
    }

    private int computePScore(LocalDateTime scheduleStartTime, LocalDateTime arrivedAt, boolean isPostConfirm) {
        if (scheduleStartTime == null || arrivedAt == null) {
            return 0;
        }
        long diffMinutes = Duration.between(scheduleStartTime, arrivedAt).toMinutes();

        if (isPostConfirm && diffMinutes >= 0 && diffMinutes <= 30) {
            return 50;
        }
        if (diffMinutes >= -10 && diffMinutes <= 3) {
            return 100;
        }
        if (diffMinutes > 3 && diffMinutes <= 15) {
            return 20;
        }
        return 0;
    }


    // 도착 시 출발 알림 업데이트
    private void updateDepartureAlarmOnArrival(
            Schedule schedule,
            RealtimeSchedule realtime,
            int predictedMinutes,
            int actualMinutes,
            ArrivedStatus arrivedStatus
    ) {
        if (schedule == null || realtime == null) {
            return;
        }
        if (predictedMinutes <= 0 || actualMinutes <= 0) {
            return;
        }

        DepartureAlarm alarm = departureAlarmRepository.findByUser(schedule.getUser()).orElse(null);
        double usfOld = alarm != null ? alarm.getUsfApplied() : 1.0;
        double ratio = (double) actualMinutes / predictedMinutes;
        double usfNew = USF_ALPHA * ratio + (1 - USF_ALPHA) * usfOld;

        int arrivalBufferMinutes = realtime.getArrivalBufferMinutes() != null
                ? realtime.getArrivalBufferMinutes()
                : getArrivalBufferMinutes(schedule);
        int notifyAtMinutes = Math.max(1, actualMinutes + arrivalBufferMinutes);

        if (alarm == null) {
            alarm = DepartureAlarm.of(schedule.getUser(), usfNew, notifyAtMinutes);
        } else {
            alarm.updateUsfAndNotifyAtMinutes(usfNew, notifyAtMinutes);
        }
        departureAlarmRepository.save(alarm);

        userMobilityLearningService.recordArrivalLearning(
                schedule.getUser(),
                schedule.getStartTime(),
                predictedMinutes,
                actualMinutes,
                arrivedStatus,
                usfOld,
                usfNew
        );
    }


    // 공통 Util 메서드들
    private Schedule findScheduleById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        return schedule;
    }

    // 일정 소유자 확인
    private void ensureOwner(Schedule schedule, Long userId) {
        if (!schedule.getUser().getId().equals(userId)) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND);
        }
    }

    private RealtimeSchedule findRealtimeSchedule(Long scheduleId) {
        return realtimeScheduleRedisService.findScheduleStatus(scheduleId);
    }

    private OdsayTransitResponse.Itinerary firstItinerary(OdsayTransitResponse response) {
        if (response == null
                || response.metaData() == null
                || response.metaData().plan() == null
                || response.metaData().plan().itineraries() == null
                || response.metaData().plan().itineraries().isEmpty()) {
            return null;
        }
        return response.metaData().plan().itineraries().get(0);
    }

    private List<Location> toWaypointLocations(List<Todo> todos) {
        if (todos == null || todos.isEmpty()) {
            return List.of();
        }
        return todos.stream()
                .filter(todo -> todo.getLatitude() != null && todo.getLongitude() != null)
                .map(todo -> new Location(todo.getLatitude(), todo.getLongitude()))
                .toList();
    }

    private List<FindRouteResponse.Waypoint> toWaypoints(List<Todo> todos) {
        if (todos == null || todos.isEmpty()) {
            return List.of();
        }
        List<FindRouteResponse.Waypoint> result = new ArrayList<>();
        int order = 1;
        for (Todo todo : todos) {
            if (todo.getLatitude() == null || todo.getLongitude() == null) {
                continue;
            }
            String name = todo.getPlaceName() != null ? todo.getPlaceName() : todo.getTitle();
            result.add(new FindRouteResponse.Waypoint(
                    name,
                    todo.getLatitude(),
                    todo.getLongitude(),
                    order++
            ));
        }
        return result;
    }


    // Redis에서 출발, 도착 여부 확인 및 에러처리 메서드
    private void ensureNotDepartedOrArrived(RealtimeSchedule realtime) {
        if (realtime == null) {
            return;
        }
        if (realtime.getActualDepartureTime() != null) {
            throw new CustomException(HomeErrorCode.ALREADY_DEPARTED);
        }
        if (realtime.getActualArrivalTime() != null) {
            throw new CustomException(HomeErrorCode.ALREADY_ARRIVED);
        }
    }

    // 일정에서 목적지 정보 존재 여부 확인
    private boolean hasDestination(Schedule schedule) {
        return schedule.getLatitude() != null && schedule.getLongitude() != null;
    }

    // 예상 출발 및 도착 시간 계산
    private ExpectedTimes computeExpectedTimes(Schedule schedule, int arrivalBufferMinutes, int etaMinutes) {
        LocalDateTime expectedArrival = schedule.getStartTime().minusMinutes(arrivalBufferMinutes);
        LocalDateTime expectedDeparture = expectedArrival.minusMinutes(etaMinutes);
        return new ExpectedTimes(expectedDeparture, expectedArrival);
    }

    // 사용자 패턴 기반 ETA 보정
    private int applyUserPattern(User user, LocalDateTime scheduleStartTime, int etaMinutes) {
        if (user == null || scheduleStartTime == null || etaMinutes <= 0) {
            return etaMinutes;
        }
        int adjustment = computeEtaAdjustment(user, scheduleStartTime);
        if (adjustment == 0) {
            return etaMinutes;
        }
        int adjusted = etaMinutes + adjustment;
        return Math.max(1, adjusted);
    }

    private int computeEtaAdjustment(User user, LocalDateTime scheduleStartTime) {
        if (user == null || scheduleStartTime == null) {
            return 0;
        }
        UserLearningPattern pattern = userLearningPatternRepository
                .findByUserAndTimeSlotAndRouteType(
                        user,
                        TimeSlot.fromHour(scheduleStartTime.getHour()),
                        app.tamingo.domain.userlearning.entity.enums.RouteType.TRANSIT
                )
                .orElse(null);

        if (pattern == null || pattern.getSampleCount() < MIN_PATTERN_SAMPLES) {
            return 0;
        }

        int raw = pattern.getAvgEtaDiff();
        int clamped = Math.max(-MAX_ETA_ADJUST_MIN, Math.min(MAX_ETA_ADJUST_MIN, raw));
        return clamped;
    }


    // 길찾기 사용 여부를 redis에 저장
    private void applyNavigationEnabled(RealtimeSchedule realtime, Schedule schedule) {
        if (realtime == null || schedule == null) {
            return;
        }
        if (schedule.getIsNavigationEnabled() == null) {
            return;
        }
        realtime.updateNavigationEnabled(schedule.getIsNavigationEnabled());
    }

    // 도착 처리
    private void handleArrivalIfNeeded(
            Schedule schedule,
            double userLat,
            double userLng,
            LocalDateTime now,
            String startPlaceName
    ) {
        if (!isArrived(schedule, userLat, userLng, 0.05)) {
            return;
        }
        recordActualArrivalAndFinalize(startPlaceName, schedule, now);

        // 지각 알림 전송
        if (isLateByMinutes(schedule.getStartTime(), now, 3)) {
            // TODO : 지각 알림 전송 추가
        }
    }

    // 길찾기 시 도착 확인 -> 100m 이내 도착 처리, 아니라면 미도착 처리
    @Transactional
    public FindRouteEndResponse confirmArrivalByEndRouteFind(StartLocationGpsRequest request, LocalDateTime now) {
        Schedule schedule = findScheduleById(request.scheduleId());
        // 이미 도착 처리가 되었는지 확인
        if (hasActualArrival(schedule.getId())) {
            throw new CustomException(HomeErrorCode.ALREADY_ARRIVED);
        }
        // 도착지 근처 100m 이내인지 확인
        if (geoService.isWithin(schedule.getLatitude(), schedule.getLongitude(),
                request.latitude(), request.longitude(), 0.1)) {
            // 도착한 것으로 처리
            markActualArrivalIfFirst(schedule.getId(), now);
            return new FindRouteEndResponse(true);
        } else {
            return new FindRouteEndResponse(false);
        }
    }

    private static final class ExpectedTimes {
        private final LocalDateTime departure;
        private final LocalDateTime arrival;

        private ExpectedTimes(LocalDateTime departure, LocalDateTime arrival) {
            this.departure = departure;
            this.arrival = arrival;
        }
    }

    @Transactional
    public void initializeRealtimeFromSnapshot(Long scheduleId, LocalDateTime now) {
        if (scheduleId == null) {
            return;
        }
        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);
        // 삭제된 스케줄이거나 존재하지 않으면 실행하지 않음, 위치 정보 없어도 추적하지 않음
        if (schedule == null || !hasDestination(schedule)) {
            return;
        }
        // 길찾기 기능 사용 하지 않을 경우
        if (!Boolean.TRUE.equals(schedule.getIsNavigationEnabled())) {
            return;
        }
        ScheduleStartSnapshotService.StartLocationSnapshotInfo snapshot =
                scheduleStartSnapshotService.findSnapshotLocation(schedule);
        if (snapshot == null) {
            return;
        }
        // 도착 시간 계산
        DirectionResult route = directionService.calculateRoute(
                snapshot.usedStartLat(),
                snapshot.usedStartLng(),
                schedule.getLatitude(),
                schedule.getLongitude()
        );
        if (route == null) {
            return;
        }

        // eta 보정 진행
        int etaMinutes = route.getTotalMinutes();
        etaMinutes = applyUserPattern(schedule.getUser(), schedule.getStartTime(), etaMinutes);
        int arrivalBufferMinutes = getArrivalBufferMinutes(schedule);
        ExpectedTimes expectedTimes = computeExpectedTimes(schedule, arrivalBufferMinutes, etaMinutes);

        saveLocation(schedule, snapshot.usedStartLat(), snapshot.usedStartLng(), now);
        saveEta(schedule, etaMinutes, arrivalBufferMinutes, expectedTimes, now);
        saveStatus(schedule, expectedTimes, now);
    }

    @Transactional
    public void initializeRealtimeOnScheduleCreate(Schedule schedule) {
        if (schedule == null || !hasDestination(schedule)) {
            return;
        }
        LocalDateTime now = virtualTimeService.now();
        long ttlSec = ttlSecondsUntil(resolveEndTime(schedule), REALTIME_SCHEDULE_TTL_AFTER_END_MIN);
        RealtimeSchedule realtime = realtimeScheduleRedisService
                .getOrCreateScheduleStatus(schedule.getId(), now.format(ISO), ttlSec);
        applyNavigationEnabled(realtime, schedule);
        realtimeScheduleRedisService.saveScheduleStatus(realtime);
    }


    @Transactional
    public void refreshRealtimeOnScheduleUpdate(Schedule schedule) {
        if (schedule == null) {
            return;
        }
        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(schedule.getId());
        if (realtime != null
                && (realtime.getActualDepartureTime() != null || realtime.getActualArrivalTime() != null)) {
            return;
        }
        realtimeScheduleRedisService.deleteScheduleStatus(schedule.getId());
        realtimeScheduleRedisService.deleteArrivalCheck(schedule.getId());
        initializeRealtimeOnScheduleCreate(schedule);
    }

    private void checkTrafficAndNotify(Schedule schedule, int etaMinutes) {
        LocalDateTime now = virtualTimeService.now();
        LocalDateTime startTime = schedule.getStartTime(); //

        if (now.isAfter(startTime.minusMinutes(11)) && now.isBefore(startTime.minusMinutes(4))) {

            if (now.plusMinutes(etaMinutes).isAfter(startTime)) {
                User user = schedule.getUser(); //

                notificationProducer.reserve(
                        NotificationMessage.createTrafficCongestion(
                                user.getId(), user.getNickname(), schedule.getPlaceName(), etaMinutes),
                        now
                );

                log.info("[4번 교통혼잡] {}님 예약", user.getNickname());
            }
        }
    }

}

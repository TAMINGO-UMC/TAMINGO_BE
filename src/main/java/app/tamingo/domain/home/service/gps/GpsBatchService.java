package app.tamingo.domain.home.service.gps;

import app.tamingo.domain.home.redis.RealtimeSchedule;
import app.tamingo.domain.home.redis.RealtimeScheduleRepository;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.notification.dto.NotificationMessage;
import app.tamingo.domain.notification.service.NotificationProducer;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * GPS 관련 배치 작업 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GpsBatchService {


    private static final double ARRIVAL_RADIUS_KM = 0.05; // 50m
    private static final int ARRIVAL_EARLY_MIN = 10;
    private static final int ARRIVAL_LATE_MIN = 3;
    private static final int LOCATION_REQUEST_WINDOW_MIN = 20;
    private static final int LOCATION_REQUEST_INTERVAL_MIN = 10;

    private final ScheduleRepository scheduleRepository;
    private final RealtimeScheduleRepository realtimeScheduleRepository;
    private final GeoService geoService;
    private final RealTimeScheduleService realTimeScheduleService;
    private final NotificationProducer notificationProducer;

    // 오늘의 일정 처리
    @Transactional
    public void processTodaySchedules() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<Schedule> schedules = scheduleRepository.findAllByStartTimeBetween(startOfDay, endOfDay);
        LocalDateTime now = LocalDateTime.now();

        for (Schedule schedule : schedules) {

            // 길찾기 비활성화 되었거나, 위도/경도 정보가 없는 경우 건너뜀
            if (schedule.getLatitude() == null
                    || schedule.getLongitude() == null
                    || !Boolean.TRUE.equals(schedule.getIsNavigationEnabled())) {
                continue;
            }
            RealtimeSchedule realtime = realtimeScheduleRepository
                    .findById(RealtimeSchedule.key(schedule.getId()))
                    .orElse(null);

            if (realtime == null || realtime.getLatitude() == null || realtime.getLongitude() == null) {
                realTimeScheduleService.triggerArrivalCheckIfNeeded(schedule, now);
                realTimeScheduleService.finalizeNoDataIfNeeded(schedule, now);
                continue;
            }

            boolean withinRadius = geoService.isWithin(
                    realtime.getLatitude(),
                    realtime.getLongitude(),
                    schedule.getLatitude(),
                    schedule.getLongitude(),
                    ARRIVAL_RADIUS_KM
            );

            if (withinRadius && isWithinArrivalWindow(schedule.getStartTime(), now)) {
                realTimeScheduleService.markActualArrivalIfFirst(schedule.getId(), now);
                realTimeScheduleService.recordPScoreForArrival(schedule, now, "GEOFENCE", false);
            }
        }
    }

    // 위치 전송 요청 푸시 발송
    @Transactional(readOnly = true)
    public void requestLocationUpdates() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now;
        LocalDateTime windowEnd = now.plusMinutes(LOCATION_REQUEST_WINDOW_MIN);

        List<Schedule> schedules = scheduleRepository.findAllByStartTimeBetween(windowStart, windowEnd);
        for (Schedule schedule : schedules) {
            long minutesUntilStart = java.time.Duration.between(now, schedule.getStartTime()).toMinutes();
            if (minutesUntilStart < 0 || minutesUntilStart > LOCATION_REQUEST_WINDOW_MIN) {
                continue;
            }
            if (minutesUntilStart % LOCATION_REQUEST_INTERVAL_MIN == 0) {
                sendLocationRequestPush(schedule);
            }
        }
    }

    // 도착 시간 창 내에 있는지 확인, 기본값: 일정 시작 10분 전 ~ 3분 후
    private boolean isWithinArrivalWindow(LocalDateTime startTime, LocalDateTime now) {
        LocalDateTime early = startTime.minusMinutes(ARRIVAL_EARLY_MIN);
        LocalDateTime late = startTime.plusMinutes(ARRIVAL_LATE_MIN);
        return !now.isBefore(early) && !now.isAfter(late);
    }

    // 위치 전송 요청 푸시 발송
    private void sendLocationRequestPush(Schedule schedule) {
        NotificationMessage msg = NotificationMessage.createSilentLocation(
                schedule.getUser().getId(),
                schedule.getUser().getNickname(),
                schedule.getId()
        );

        notificationProducer.send(msg);
        log.info("[5번 위치 확인] {}님 일정(Id:{})위치 요청", schedule.getUser().getNickname(), schedule.getId());
    }


}

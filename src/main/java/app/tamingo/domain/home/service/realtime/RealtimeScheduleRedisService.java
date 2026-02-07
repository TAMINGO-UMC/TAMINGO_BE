package app.tamingo.domain.home.service.realtime;

import app.tamingo.domain.home.dto.DailyScheduleResponse;
import app.tamingo.domain.home.redis.RealtimeActiveSchedule;
import app.tamingo.domain.home.redis.RealtimeActiveScheduleRepository;
import app.tamingo.domain.home.redis.RealtimeSchedule;
import app.tamingo.domain.home.redis.RealtimeScheduleArrivalCheck;
import app.tamingo.domain.home.redis.RealtimeScheduleArrivalCheckRepository;
import app.tamingo.domain.home.redis.RealtimeScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RealtimeScheduleRedisService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final RealtimeScheduleRepository realtimeScheduleRepository;
    private final RealtimeScheduleArrivalCheckRepository arrivalCheckRepository;
    private final RealtimeActiveScheduleRepository activeScheduleRepository;

    public RealtimeSchedule findScheduleStatus(Long scheduleId) {
        String key = RealtimeSchedule.key(scheduleId);
        return realtimeScheduleRepository.findById(key).orElse(null);
    }

    public RealtimeSchedule getOrCreateScheduleStatus(Long scheduleId, String updatedAt, long ttlSec) {
        RealtimeSchedule realtime = findScheduleStatus(scheduleId);
        if (realtime == null) {
            realtime = RealtimeSchedule.create(scheduleId, updatedAt, ttlSec);
        }
        return realtime;
    }

    public void saveScheduleStatus(RealtimeSchedule realtime) {
        realtimeScheduleRepository.save(realtime);
    }

    public void deleteScheduleStatus(Long scheduleId) {
        String key = RealtimeSchedule.key(scheduleId);
        realtimeScheduleRepository.deleteById(key);
    }

    public RealtimeActiveSchedule findActiveSchedule(Long userId) {
        String key = RealtimeActiveSchedule.key(userId);
        return activeScheduleRepository.findById(key).orElse(null);
    }

    public RealtimeActiveSchedule getOrCreateActiveSchedule(
            Long userId,
            Long scheduleId,
            String updatedAt,
            long ttlSec
    ) {
        RealtimeActiveSchedule active = findActiveSchedule(userId);
        if (active == null) {
            active = RealtimeActiveSchedule.create(userId, scheduleId, updatedAt, ttlSec);
            return active;
        }
        active.update(scheduleId, updatedAt, ttlSec);
        return active;
    }

    public void saveActiveSchedule(RealtimeActiveSchedule active) {
        activeScheduleRepository.save(active);
    }

    public void deleteActiveSchedule(Long userId) {
        String key = RealtimeActiveSchedule.key(userId);
        activeScheduleRepository.deleteById(key);
    }

    public boolean hasActualArrival(Long scheduleId) {
        RealtimeSchedule realtime = findScheduleStatus(scheduleId);
        return realtime != null && realtime.getActualArrivalTime() != null;
    }

    public DailyScheduleResponse.ScheduleStatusResponse toStatusResponse(RealtimeSchedule realtime) {
        if (realtime == null) {
            return null;
        }
        LocalTime expectedDeparture = parseLocalTime(realtime.getExpectedDepartureTime());
        LocalTime expectedArrival = parseLocalTime(realtime.getExpectedArrivalTime());
        return new DailyScheduleResponse.ScheduleStatusResponse(
                realtime.getCurrentStatus(),
                realtime.isStarted(),
                realtime.getLeftOrDelayMinutes(),
                expectedDeparture,
                expectedArrival,
                realtime.getLateArrivalMinutes()
        );
    }

    private LocalTime parseLocalTime(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDateTime.parse(dateTime, ISO).toLocalTime();
    }

    public RealtimeScheduleArrivalCheck findArrivalCheck(Long scheduleId) {
        String key = RealtimeScheduleArrivalCheck.key(scheduleId);
        return arrivalCheckRepository.findById(key).orElse(null);
    }

    public RealtimeScheduleArrivalCheck getOrCreateArrivalCheck(Long scheduleId, long ttlSec) {
        RealtimeScheduleArrivalCheck check = findArrivalCheck(scheduleId);
        if (check == null) {
            check = RealtimeScheduleArrivalCheck.create(scheduleId, ttlSec);
        }
        return check;
    }

    public void saveArrivalCheck(RealtimeScheduleArrivalCheck check) {
        arrivalCheckRepository.save(check);
    }

    public void deleteArrivalCheck(Long scheduleId) {
        String key = RealtimeScheduleArrivalCheck.key(scheduleId);
        arrivalCheckRepository.deleteById(key);
    }
}

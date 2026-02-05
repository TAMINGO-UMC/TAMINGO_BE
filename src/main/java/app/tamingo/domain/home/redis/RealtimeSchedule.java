package app.tamingo.domain.home.redis;

import app.tamingo.domain.home.entity.enums.CurrentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor
@RedisHash("realtime:schedule")
public class RealtimeSchedule {

    @Id
    private String key;

    private Long scheduleId;
    private CurrentStatus currentStatus;
    private boolean isStarted;
    private Integer leftOrDelayMinutes;
    private Integer lateArrivalMinutes;
    private Integer etaMinutes;
    private Integer arrivalBufferMinutes;
    private String expectedDepartureTime;
    private String expectedArrivalTime;
    private String actualDepartureTime;
    private String actualArrivalTime;
    private Double latitude;
    private Double longitude;
    private Boolean navigationEnabled;
    private String updatedAt;

    @TimeToLive
    private Long ttlSec;

    public static String key(Long scheduleId) {
        return "schedule:" + scheduleId;
    }

    public static RealtimeSchedule create(Long scheduleId, String updatedAt, long ttlSec) {
        RealtimeSchedule realtime = new RealtimeSchedule();
        realtime.key = key(scheduleId);
        realtime.scheduleId = scheduleId;
        realtime.updatedAt = updatedAt;
        realtime.ttlSec = ttlSec;
        return realtime;
    }

    public void applyStatus(
            CurrentStatus currentStatus,
            boolean isStarted,
            Integer leftOrDelayMinutes,
            Integer lateArrivalMinutes,
            String expectedDepartureTime,
            String expectedArrivalTime,
            String updatedAt,
            long ttlSec
    ) {
        this.currentStatus = currentStatus;
        this.isStarted = isStarted;
        this.leftOrDelayMinutes = leftOrDelayMinutes;
        this.lateArrivalMinutes = lateArrivalMinutes;
        this.expectedDepartureTime = expectedDepartureTime;
        this.expectedArrivalTime = expectedArrivalTime;
        this.updatedAt = updatedAt;
        this.ttlSec = ttlSec;
    }

    public void applyEta(
            Integer etaMinutes,
            Integer arrivalBufferMinutes,
            String expectedDepartureTime,
            String expectedArrivalTime,
            String updatedAt,
            long ttlSec
    ) {
        this.etaMinutes = etaMinutes;
        this.arrivalBufferMinutes = arrivalBufferMinutes;
        this.expectedDepartureTime = expectedDepartureTime;
        this.expectedArrivalTime = expectedArrivalTime;
        this.updatedAt = updatedAt;
        this.ttlSec = ttlSec;
    }

    public void applyLocation(
            Double latitude,
            Double longitude,
            String updatedAt,
            long ttlSec
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.updatedAt = updatedAt;
        this.ttlSec = ttlSec;
    }

    public void updateActualDepartureTime(String actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    public void updateActualArrivalTime(String actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public void updateNavigationEnabled(Boolean navigationEnabled) {
        this.navigationEnabled = navigationEnabled;
    }
}

package app.tamingo.domain.home.redis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor
@RedisHash("realtime:active-schedule")
public class RealtimeActiveSchedule {

    @Id
    private String key;

    private Long userId;
    private Long scheduleId;
    private String updatedAt;

    @TimeToLive
    private Long ttlSec;

    public static String key(Long userId) {
        return "active-schedule:user:" + userId;
    }

    public static RealtimeActiveSchedule create(Long userId, Long scheduleId, String updatedAt, long ttlSec) {
        RealtimeActiveSchedule active = new RealtimeActiveSchedule();
        active.key = key(userId);
        active.userId = userId;
        active.scheduleId = scheduleId;
        active.updatedAt = updatedAt;
        active.ttlSec = ttlSec;
        return active;
    }

    public void update(Long scheduleId, String updatedAt, long ttlSec) {
        this.scheduleId = scheduleId;
        this.updatedAt = updatedAt;
        this.ttlSec = ttlSec;
    }
}

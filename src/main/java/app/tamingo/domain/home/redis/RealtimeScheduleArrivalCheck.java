package app.tamingo.domain.home.redis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor
@RedisHash("realtime:arrival-check")
public class RealtimeScheduleArrivalCheck {

    @Id
    private String key;

    private Long scheduleId;
    private String fallbackPushSentAt;
    private Boolean arrivedConfirmed;
    private String confirmedAt;
    private Integer pScore;
    private String pScoreSource;
    private String pScoreAppliedAt;

    @TimeToLive
    private Long ttlSec;

    public static String key(Long scheduleId) {
        return "arrival-check:" + scheduleId;
    }

    public static RealtimeScheduleArrivalCheck create(Long scheduleId, long ttlSec) {
        RealtimeScheduleArrivalCheck check = new RealtimeScheduleArrivalCheck();
        check.key = key(scheduleId);
        check.scheduleId = scheduleId;
        check.ttlSec = ttlSec;
        return check;
    }

    public void markFallbackPushSent(String sentAt, long ttlSec) {
        this.fallbackPushSentAt = sentAt;
        this.ttlSec = ttlSec;
    }

    public void confirmArrival(String confirmedAt, long ttlSec) {
        this.arrivedConfirmed = true;
        this.confirmedAt = confirmedAt;
        this.ttlSec = ttlSec;
    }

    public void applyPScore(int pScore, String source, String appliedAt, long ttlSec) {
        this.pScore = pScore;
        this.pScoreSource = source;
        this.pScoreAppliedAt = appliedAt;
        this.ttlSec = ttlSec;
    }
}

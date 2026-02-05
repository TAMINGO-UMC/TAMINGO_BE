package app.tamingo.domain.home.service.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ScheduleInitQueueService {

    // ZSET 일정 초기화 큐 - 일정 시작 20분 전에 생성
    private static final String KEY = "zset:schedule:init";
    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final StringRedisTemplate stringRedisTemplate;

    public void scheduleInit(Long scheduleId, LocalDateTime runAt) {
        if (scheduleId == null || runAt == null) {
            return;
        }
        double score = runAt.atZone(TARGET_ZONE).toEpochSecond();
        stringRedisTemplate.opsForZSet().add(KEY, scheduleId.toString(), score);
    }

    public List<Long> fetchDue(LocalDateTime now, int limit) {
        if (now == null) {
            return List.of();
        }
        double score = now.atZone(TARGET_ZONE).toEpochSecond();
        Set<String> members = stringRedisTemplate.opsForZSet()
                .rangeByScore(KEY, Double.NEGATIVE_INFINITY, score, 0, limit);
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        stringRedisTemplate.opsForZSet().remove(KEY, members.toArray());
        List<Long> result = new ArrayList<>(members.size());
        for (String member : members) {
            try {
                result.add(Long.parseLong(member));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }
}

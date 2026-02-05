package app.tamingo.domain.notification.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class NotificationRedisRepository {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY = "notification:reservation:queue";

    // ZSet에 데이터 추가
    public void saveToZSet(Object value, double score) {
        try {
            // 객체를 직접 JSON 문자열로 변환해서 저장
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForZSet().add(KEY, jsonValue, score);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 저장 중 JSON 변환 실패", e);
        }
    }

    // 발송 예정 시간 내의 데이터 조회
    public Set<String> rangeByScore(double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(KEY, min, max);
    }

    // 발송 완료된 데이터 삭제
    public void remove(Object value) {
        redisTemplate.opsForZSet().remove(KEY, value);
    }
}

package app.tamingo.domain.notification.service;

import app.tamingo.domain.notification.dto.NotificationMessage;
import app.tamingo.domain.notification.repository.NotificationRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String NOTIFICATION_QUEUE_KEY = "notification:reservation:queue";
    private final NotificationRedisRepository notificationRedisRepository;

    public void reserve(NotificationMessage message, LocalDateTime targetTime) {
        try {
            // 발송 시간을 초 단위 타임스탬프로 변환
            double score = (double) targetTime.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

            // Redis ZSet에 추가
            notificationRedisRepository.saveToZSet(message, score);
            log.info("[알림 예약 성공] 유저: {}, 타입: {}, 발송예정: {}",
                    message.getUserName(), message.getType(), targetTime);

        } catch (Exception e) {
            log.error("[알림 예약 실패] 유저ID: {}, 에러: {}", message.getUserId(), e.getMessage());
            throw new RuntimeException("Redis 알림 예약 중 오류 발생", e);
        }
    }

    public void send(NotificationMessage message) {
        try {
            // 즉시 발송
            double score = (double) LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();

            // Redis ZSet에 추가
            notificationRedisRepository.saveToZSet(message, score);

            log.info("[알림 즉시 발송 요청] 유저: {}, 타입: {}, 시간: {}",
                    message.getUserName(), message.getType(), LocalDateTime.now());

        } catch (Exception e) {
            log.error("[알림 발송 실패] 유저ID: {}, 에러: {}", message.getUserId(), e.getMessage());
            throw new RuntimeException("Redis 알림 발송 중 오류 발생", e);
        }
    }

}

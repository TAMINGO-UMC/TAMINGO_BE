package app.tamingo.domain.notification.scheduler;

import app.tamingo.domain.notification.service.NotificationConsumer; // 실제 FCM 쏘는 서비스
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPushScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationConsumer notificationConsumer;

    private static final String NOTIFICATION_QUEUE_KEY = "notification:reservation:queue";

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void publishNotifications() {
        if (isQuietHours()) {
            return;
        }
        // 현재 시간 타임스탬프 기준으로 발송 대상 가져오기
        double now = (double) Instant.now().getEpochSecond();

        // 0부터 현재 시간 사이의 스코어를 가진 발송 대상 조회
        Set<String> messages = redisTemplate.opsForZSet().rangeByScore(NOTIFICATION_QUEUE_KEY, 0, now);

        if (messages == null || messages.isEmpty()) {
            return;
        }

        log.info("발송 대상 알림 : {}건 ", messages.size());

        for (String jsonMessage : messages) {
            try {
                // JSON 문자열을 Consumer로 넘겨 실제 발송 처리 (객체 변환 포함)
                notificationConsumer.sendPushFromJson(jsonMessage);

                // 발송 처리 성공 시 Redis 큐에서 제거
                redisTemplate.opsForZSet().remove(NOTIFICATION_QUEUE_KEY, jsonMessage);

                log.info("알림 발송 시도 완료");
            } catch (Exception e) {
                // 발송 실패 시 삭제하지 않고 다음 주기에 재시도할 수 있도록 로그만 남김
                log.error("알림 발송 중 오류 발생: {}", e.getMessage());
            }
        }
    }

    private boolean isQuietHours() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        return !now.isBefore(LocalTime.of(1, 0)) && now.isBefore(LocalTime.of(8, 0));
    }
}

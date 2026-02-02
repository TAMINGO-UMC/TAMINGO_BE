package app.tamingo.domain.notification.service;

import app.tamingo.domain.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RedissonClient redisson;

    // 알림 메시지 큐 이름
    private static String NOTIFICATION_QUEUE_NAME = "notification_queue";

    // 알림 예약 메서드
    public void sendNotification(NotificationMessage message, long delayMinutes) {

        // 실제 메시지가 적재되는 Blocking Queue
        RBlockingQueue<NotificationMessage> blockingQueue = redisson.getBlockingQueue(NOTIFICATION_QUEUE_NAME);

        // 지연 기능을 담당하는 Delayed Queue
        RDelayedQueue<NotificationMessage> delayedQueue = redisson.getDelayedQueue(blockingQueue);

        // 지연시간이 지나면 메시지가 blockingQueue로 넘어가게끔 예약
        delayedQueue.offer(message, delayMinutes, TimeUnit.MINUTES);
    }

}

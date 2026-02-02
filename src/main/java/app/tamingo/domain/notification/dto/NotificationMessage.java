package app.tamingo.domain.notification.dto;

import app.tamingo.domain.notification.enums.NotificationType;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class NotificationMessage implements Serializable {

    private Long userId;
    private String userName;
    private String destination;
    private NotificationType type;
    private int expectedEta;
    private int offsetMinutes;

    // 출발 전 알림
    public static NotificationMessage createDepartureBefore(
            Long userId, String userName,
            String destination, int expectedEta,
            int offsetMinutes) {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.BEFORE_CUSTOM_MIN)
                .userName(userName)
                .destination(destination)
                .expectedEta(expectedEta)
                .offsetMinutes(offsetMinutes)
                .build();
    }

    // 일반 알림 (정시 출발)
    public static NotificationMessage createGeneral(
            Long userId, String userName,
            String destination, int expectedEta
    )   {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.GENERAL)
                .userName(userName)
                .destination(destination)
                .expectedEta(expectedEta)
                .offsetMinutes(0)
                .build();
    }

    // 도착 확인 알림 (사후 체크)
    public static NotificationMessage createArrival(
            Long userId, String userName, String destination
    ) {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.ARRIVAL_CHECK)
                .userName(userName)
                .destination(destination)
                .build();
    }
}

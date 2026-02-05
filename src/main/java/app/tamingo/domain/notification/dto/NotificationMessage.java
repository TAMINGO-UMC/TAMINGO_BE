package app.tamingo.domain.notification.dto;

import app.tamingo.domain.notification.enums.NotificationType;
import lombok.*;

import java.io.Serializable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class NotificationMessage implements Serializable {

    private Long userId;
    private String userName;
    private String destination;
    private NotificationType type;
    private int expectedEta;

    // [1번 알림] 출발 20분 전 알림
    public static NotificationMessage createDepartureBefore(
            Long userId, String userName,
            String destination, int expectedEta) {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.BEFORE_20MIN)
                .userName(userName)
                .destination(destination)
                .expectedEta(expectedEta)
                .build();
    }

    // [2번 알림] 일반 알림 (정시 출발)
    public static NotificationMessage createGeneral(
            Long userId, String userName,
            String destination, int expectedEta)   {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.GENERAL)
                .userName(userName)
                .destination(destination)
                .expectedEta(expectedEta)
                .build();
    }

    // [3번] 개인화 정시 출발 알림 (USF 적용)
    public static NotificationMessage createCustom(
            Long userId, String userName, String destination, int expectedEta) {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.PERSONAL)
                .userName(userName)
                .destination(destination)
                .expectedEta(expectedEta)
                .build();
    }
}

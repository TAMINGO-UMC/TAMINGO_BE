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
    private Long scheduleId;
    private String todoTitle;

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

    // [4번] 교통 혼잡 알림
    public static NotificationMessage createTrafficCongestion(
            Long userId, String userName, String destination, int expectedEta) {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.TRAFFIC)
                .userName(userName)
                .destination(destination)
                .expectedEta(expectedEta)
                .build();
    }

    // [6번] 도착 확인 알림
    public static NotificationMessage createArrival(
            Long userId, String userName, String destination, int expectedEta) {
        return NotificationMessage.builder()
                .userId(userId)
                .type(NotificationType.ARRIVAL_CHECK)
                .userName(userName)
                .destination(destination)
                .expectedEta(expectedEta)
                .build();
    }


    public boolean isSilent() {
        return this.type == NotificationType.SILENT_LOCATION ||
                this.type == NotificationType.SILENT_GPS;
    }

    // [5번] 위치 전송 (정적)
    public static NotificationMessage createSilentLocation(Long userId, String userName, Long scheduleId) {
        return NotificationMessage.builder()
                .userId(userId)
                .userName(userName)
                .scheduleId(scheduleId)
                .type(NotificationType.SILENT_LOCATION)
                .build();
    }

    // [7번] Silent GPS
    public static NotificationMessage createSilentGps(Long userId, String userName, Long scheduleId) {
        return NotificationMessage.builder()
                .userId(userId)
                .userName(userName)
                .scheduleId(scheduleId)
                .type(NotificationType.SILENT_GPS)
                .build();
    }

    // [8번] 틈새시간 알림
    public static NotificationMessage createGapTime(
            Long userId, String userName, String todoTitle, int gapMinutes
    )  {
        return NotificationMessage.builder()
                .userId(userId)
                .userName(userName)
                .destination(todoTitle)
                .expectedEta(gapMinutes)
                .type(NotificationType.GAP)
                .build();
    }

    // [11번] 연계 알림
    public static NotificationMessage createRouteLink(Long userId, String userName, String destination, String todoTitle) {
        return NotificationMessage.builder()
                .userId(userId)
                .userName(userName)
                .type(NotificationType.ROUTE)
                .destination(destination)
                .expectedEta(0)
                .todoTitle(todoTitle)
                .build();

    }
}

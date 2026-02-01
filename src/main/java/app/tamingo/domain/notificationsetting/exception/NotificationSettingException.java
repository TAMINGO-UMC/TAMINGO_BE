package app.tamingo.domain.notificationsetting.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationSettingException implements BaseCode {

    NOTIFICATION_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION-001", "해당 알림 설정을 찾을 수 없습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

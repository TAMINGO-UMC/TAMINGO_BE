package app.tamingo.domain.useractivetime.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserActiveTimeError implements BaseCode {

    TIME_ORDER_INVALID(HttpStatus.BAD_REQUEST, "TIME-001", "종료 시간은 시작 시간보다 이후여야 합니다."),
    TIME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "TIME-002", "활동 시간 형식이 올바르지 않습니다. (HH:mm)");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

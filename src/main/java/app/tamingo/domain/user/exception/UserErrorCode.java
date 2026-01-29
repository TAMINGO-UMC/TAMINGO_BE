package app.tamingo.domain.user.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 사용자입니다."),
    USER_ONBOARDING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "USER-003", "온보딩이 완료되지 않았습니다."),
    USER_ALREADY_ONBOARDED(HttpStatus.BAD_REQUEST, "USER-004", "이미 온보딩이 완료된 사용자입니다."),
    USER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "USER-005", "사용자 상태가 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

package app.tamingo.domain.transportpreference.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TransportPreferenceErrorCode implements BaseCode {

    TRANSPORT_PREFERENCES_INVALID(HttpStatus.BAD_REQUEST, "TRANSPORT-001", "이동 수단 선호 설정이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
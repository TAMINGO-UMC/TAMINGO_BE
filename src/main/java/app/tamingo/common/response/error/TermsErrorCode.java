package app.tamingo.common.response.error;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TermsErrorCode implements BaseCode {

    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS-001", "해당 약관을 찾을 수 없습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS-002", "필수 약관에 동의하지 않았습니다."),
    INVALID_TERMS_REQUEST(HttpStatus.BAD_REQUEST, "TERMS-003", "약관 요청이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
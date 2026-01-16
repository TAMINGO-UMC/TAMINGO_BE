package app.tamingo.common.response;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {

    // 약관
    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS-001", "해당 약관을 찾을 수 없습니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "TERMS-002", "필수 약관에 동의하지 않았습니다."),
    INVALID_TERMS_REQUEST(HttpStatus.BAD_REQUEST, "TERMS-003", "약관 요청이 올바르지 않습니다."),

    // 회원 가입
    SIGNUP_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-001", "회원가입 세션이 만료되었거나 존재하지 않습니다."),
    SIGNUP_STEP_INVALID(HttpStatus.CONFLICT, "AUTH-002", "회원가입 진행 단계가 올바르지 않습니다."),
    SIGNUP_EMAIL_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-003", "인증번호가 만료되었거나 존재하지 않습니다."),
    SIGNUP_EMAIL_CODE_INVALID(HttpStatus.BAD_REQUEST, "AUTH-004", "인증번호가 올바르지 않습니다."),
    SIGNUP_EMAIL_NOT_MATCHED(HttpStatus.BAD_REQUEST, "AUTH-005", "요청 이메일이 세션 정보와 일치하지 않습니다."),
    SIGNUP_EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH-006", "이메일 인증이 완료되지 않았습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH-007", "이미 가입된 이메일입니다."),

    // 서버 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON-002", "요청 파라미터가 올바르지 않습니다."),
    INVALID_DATE(HttpStatus.BAD_REQUEST, "DATE-001", "유효하지 않은 날짜입니다."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, "FORMAT-001", "형식이 올바르지 않습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "COMMON-003", "JSON 파싱에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

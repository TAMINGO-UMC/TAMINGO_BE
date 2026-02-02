package app.tamingo.domain.auth.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseCode {

    // 회원 가입
    SIGNUP_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-001", "회원가입 세션이 만료되었거나 존재하지 않습니다."),
    SIGNUP_STEP_INVALID(HttpStatus.CONFLICT, "AUTH-002", "회원가입 진행 단계가 올바르지 않습니다."),
    SIGNUP_EMAIL_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-003", "인증번호가 만료되었거나 존재하지 않습니다."),
    SIGNUP_EMAIL_CODE_INVALID(HttpStatus.BAD_REQUEST, "AUTH-004", "인증번호가 올바르지 않습니다."),
    SIGNUP_EMAIL_NOT_MATCHED(HttpStatus.BAD_REQUEST, "AUTH-005", "요청 이메일이 세션 정보와 일치하지 않습니다."),
    SIGNUP_EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "AUTH-006", "이메일 인증이 완료되지 않았습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "AUTH-007", "이미 가입된 이메일입니다."),
    SIGNUP_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH-008", "닉네임은 필수입니다."),
    SIGNUP_NICKNAME_TOO_LONG(HttpStatus.BAD_REQUEST, "AUTH-009", "닉네임은 최대 10자까지 가능합니다."),
    SIGNUP_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH-010", "비밀번호는 필수입니다."),
    SIGNUP_PASSWORD_POLICY_INVALID(HttpStatus.BAD_REQUEST, "AUTH-011", "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자만 가능합니다."),

    // 로그인
    LOGIN_EMAIL_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "AUTH-020", "이메일 형식이 올바르지 않습니다."),
    LOGIN_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-021", "존재하지 않는 이메일입니다."),
    LOGIN_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "AUTH-022", "비밀번호가 올바르지 않습니다."),
    LOGIN_PASSWORD_NOT_SET(HttpStatus.BAD_REQUEST, "AUTH-023", "비밀번호가 설정되지 않은 계정입니다."),

    TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH-030", "토큰이 필요합니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-031", "토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-032", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH-033", "리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH-034", "리프레시 토큰이 일치하지 않습니다."),

    KAKAO_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "AUTH-040", "카카오 액세스 토큰이 유효하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
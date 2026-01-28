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
    SIGNUP_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH-008", "닉네임은 필수입니다."),
    SIGNUP_NICKNAME_TOO_LONG(HttpStatus.BAD_REQUEST, "AUTH-009", "닉네임은 최대 10자까지 가능합니다."),
    SIGNUP_PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "AUTH-010", "비밀번호는 필수입니다."),
    SIGNUP_PASSWORD_POLICY_INVALID(HttpStatus.BAD_REQUEST, "AUTH-011", "비밀번호는 8~16자의 영문 대소문자, 숫자, 특수문자만 가능합니다."),


    // 로그인
    LOGIN_EMAIL_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "AUTH-020", "이메일 형식이 올바르지 않습니다."),
    LOGIN_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-021", "존재하지 않는 이메일입니다."),
    LOGIN_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "AUTH-022", "비밀번호가 올바르지 않습니다."),
    LOGIN_PASSWORD_NOT_SET(HttpStatus.BAD_REQUEST, "AUTH-023", "비밀번호가 설정되지 않은 계정입니다."),

    TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH-031", "토큰이 필요합니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-032", "토큰이 유효하지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH-033", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH-034", "리프레시 토큰이 존재하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH-035", "리프레시 토큰이 일치하지 않습니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER-002", "이미 존재하는 사용자입니다."),
    USER_ONBOARDING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "USER-003", "온보딩이 완료되지 않았습니다."),
    USER_ALREADY_ONBOARDED(HttpStatus.BAD_REQUEST, "USER-004", "이미 온보딩이 완료된 사용자입니다."),
    USER_STATUS_INVALID(HttpStatus.BAD_REQUEST, "USER-005", "사용자 상태가 유효하지 않습니다."),

    // 온보딩 초기 세팅
    ONBOARDING_ACTIVE_TIME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "ONB-001", "활동 시간 형식이 올바르지 않습니다. (HH:mm)"),
    ONBOARDING_ACTIVE_TIME_RANGE_INVALID(HttpStatus.BAD_REQUEST, "ONB-002", "활동 종료 시간은 시작 시간 이후여야 합니다."),
    ONBOARDING_FAVORITE_PLACES_EMPTY(HttpStatus.BAD_REQUEST, "ONB-003", "자주 가는 장소는 최소 1개 이상 등록해야 합니다."),
    ONBOARDING_FAVORITE_PLACES_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ONB-004", "자주 가는 장소는 최대 5개까지 등록할 수 있습니다."),
    ONBOARDING_TRANSPORT_PREFERENCES_INVALID(HttpStatus.BAD_REQUEST, "ONB-005", "이동 수단 선호 설정이 올바르지 않습니다."),
    ONBOARDING_NOTIFICATION_MINUTE_REQUIRED(HttpStatus.BAD_REQUEST, "ONB-006", "알림이 활성화된 경우, 목표 도착 시간을 선택해야 합니다."),

    // 자주 가는 장소
    FAVORITE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE-001", "해당 장소를 찾을 수 없습니다."),
    FAVORITE_PLACE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PLACE-002", "이미 등록된 장소명 또는 주소입니다."),

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

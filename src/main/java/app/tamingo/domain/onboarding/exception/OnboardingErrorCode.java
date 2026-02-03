package app.tamingo.domain.onboarding.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OnboardingErrorCode implements BaseCode {

    ONBOARDING_ACTIVE_TIME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "ONB-001", "활동 시간 형식이 올바르지 않습니다. (HH:mm)"),
    ONBOARDING_ACTIVE_TIME_RANGE_INVALID(HttpStatus.BAD_REQUEST, "ONB-002", "활동 종료 시간은 시작 시간 이후여야 합니다."),
    ONBOARDING_TRANSPORT_PREFERENCES_INVALID(HttpStatus.BAD_REQUEST, "ONB-005", "이동 수단 선호 설정이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

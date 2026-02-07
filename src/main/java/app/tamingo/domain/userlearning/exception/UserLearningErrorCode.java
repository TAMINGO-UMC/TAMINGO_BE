package app.tamingo.domain.userlearning.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum UserLearningErrorCode implements BaseCode {

    PERSONAL_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "USERLEARNING-001", "오차로그 설정이 되지 않은 사용자입니다."),
    USER_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND,"USERLEARNING-002","개인화 요약이 존재하지 않습니다." );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

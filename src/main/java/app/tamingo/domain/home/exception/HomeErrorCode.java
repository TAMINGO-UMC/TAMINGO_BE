package app.tamingo.domain.home.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HomeErrorCode implements BaseCode {

    DAILY_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "HOME-001", "오늘의 일정이 존재하지 않습니다."),
    SUGGESTION_LEARNING_NOT_FOUND(HttpStatus.NOT_FOUND, "HOME-002", "추천 학습 일정이 존재하지 않습니다."),
    SUGGESTION_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "HOME-003","추천 학습 일정 접근 권한이 없습니다." );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

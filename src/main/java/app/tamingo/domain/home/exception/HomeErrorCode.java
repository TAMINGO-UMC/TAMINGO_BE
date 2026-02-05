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
    SUGGESTION_UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "HOME-003","추천 학습 일정 접근 권한이 없습니다."),
    ALREADY_DEPARTED(HttpStatus.CONFLICT, "HOME-004", "이미 출발 처리된 일정입니다."),
    ALREADY_ARRIVED(HttpStatus.CONFLICT,"HOME-005","이미 도착 처리된 일정입니다." ),
    SCHEDULE_START_SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND,"HOME-006" ,"해당 일정에 저장된 출발지가 없습니다." ),
    TOO_MANY_TODOS(HttpStatus.BAD_REQUEST,"HOME-007", "경유 가능한 경유지의 개수를 초과했습니다." );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

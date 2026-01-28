package app.tamingo.domain.schedule.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements BaseCode {

    SCHEDULE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-001", "해당 스케줄 카테고리를 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE-002", "해당 스케줄을 찾을 수 없습니다."),
    SCHEDULE_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "SCHEDULE-003", "요청 파라미터가 올바르지 않습니다."),
    SCHEDULE_INVALID_DATE(HttpStatus.BAD_REQUEST, "SCHEDULE-004", "유효하지 않은 날짜입니다."),
    SCHEDULE_CATEGORY_DUPLICATED(HttpStatus.CONFLICT , "SCHEDULE-005","중복된 명칭 입니다."),
    SCHEDULE_CATEGORY_IN_USE(HttpStatus.CONFLICT,"SCHEDULE-006","해당 카테고리를 사용하는 일정이 존재하여 삭제할 수 없습니다.");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}

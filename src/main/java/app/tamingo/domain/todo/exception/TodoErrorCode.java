package app.tamingo.domain.todo.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TodoErrorCode implements BaseCode {

    TODO_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO-001", "해당 할일 카테고리를 찾을 수 없습니다."),
    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "TODO-002", "해당 할일을 찾을 수 없습니다."),
    TODO_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "TODO-003", "요청 파라미터가 올바르지 않습니다."),
    TODO_CATEGORY_DUPLICATED(HttpStatus.CONFLICT, "TODO-005", "중복된 명칭 입니다."),
    TODO_CATEGORY_IN_USE(HttpStatus.CONFLICT, "TODO-006", "해당 카테고리를 사용하는 할일이 존재하여 삭제할 수 없습니다."),
    TODO_NOT_OWNER(HttpStatus.BAD_REQUEST, "TODO-007","해당 할 일의 작성자가 아닙니다."),
    TODO_CONNECT_ONE(HttpStatus.BAD_REQUEST,"TODO-008","일정 하나만 연결 가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

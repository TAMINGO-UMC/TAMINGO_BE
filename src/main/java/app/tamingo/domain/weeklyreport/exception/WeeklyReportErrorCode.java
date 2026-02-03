package app.tamingo.domain.weeklyreport.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WeeklyReportErrorCode implements BaseCode {

    WEEKLY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "WEEKLY-001", "해당 주차의 리포트가 아직 생성되지 않았습니다."),
    WEEKLY_REPORT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "WEEKLY-002", "요청 파라미터가 올바르지 않습니다."),

    // GPT (배치 로그/검증용)
    WEEKLY_REPORT_GPT_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "WEEKLY-101", "주간 인사이트 응답 형식이 올바르지 않습니다."),
    WEEKLY_REPORT_GPT_INVALID_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "WEEKLY-102", "주간 인사이트 타입이 올바르지 않습니다."),
    WEEKLY_REPORT_GPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WEEKLY-103", "주간 인사이트 생성에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

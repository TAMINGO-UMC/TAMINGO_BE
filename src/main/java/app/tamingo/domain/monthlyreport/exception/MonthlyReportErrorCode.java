package app.tamingo.domain.monthlyreport.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MonthlyReportErrorCode implements BaseCode {

    MONTHLY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "MONTHLY-001", "해당 월의 리포트가 아직 생성되지 않았습니다."),
    MONTHLY_REPORT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "MONTHLY-002", "요청 파라미터가 올바르지 않습니다."),

    // GPT 관련 (배치에서 내부적으로 쓰고, 실패해도 배치는 계속)
    MONTHLY_REPORT_GPT_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "MONTHLY-101", "월간 인사이트 응답 형식이 올바르지 않습니다."),
    MONTHLY_REPORT_GPT_INVALID_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "MONTHLY-102", "월간 인사이트 타입이 올바르지 않습니다."),
    MONTHLY_REPORT_GPT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MONTHLY-103", "월간 인사이트 생성에 실패했습니다.");
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

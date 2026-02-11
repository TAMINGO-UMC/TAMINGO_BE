package app.tamingo.domain.odsay.exception;

import app.tamingo.common.response.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum OdsayErrorCode implements BaseCode {

    DISTANCE_TOO_SHORT(HttpStatus.BAD_REQUEST, "ODSAY-001", "거리가 너무 짧아 경로를 계산할 수 없습니다."),
    REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "ODSAY-002", "ODSAY 요청에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

package app.tamingo.common.exception;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.BaseCode;
import app.tamingo.common.response.ErrorCode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    private final List<DataIntegrityMapper> dataIntegrityMappers;

    public ExceptionAdvice(Optional<List<DataIntegrityMapper>> mappers) {
        this.dataIntegrityMappers = mappers.orElseGet(List::of);
    }

    // @RequestParam, @PathVariable 등 Bean Validation 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e, WebRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");

        ApiResponse<Object> body = ApiResponse.onFailure(
                ErrorCode.INVALID_REQUEST,
                message
        );
        return handleExceptionInternal(e, body, new HttpHeaders(),
                ErrorCode.INVALID_REQUEST.getHttpStatus(),
                request);
    }

    // @Valid @RequestBody DTO 검증 실패
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String field = fieldError.getField();
            String msg = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
            errors.merge(field, msg, (a, b) -> a + ", " + b);
        });

        ApiResponse<Object> body = ApiResponse.onFailure(
                ErrorCode.INVALID_REQUEST,
                errors
        );
        return handleExceptionInternal(e, body, headers,
                ErrorCode.INVALID_REQUEST.getHttpStatus(),
                request);
    }

    // 도메인 CustomException
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException e, HttpServletRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(e.getErrorCode(),null);
        WebRequest webRequest = new ServletWebRequest(request);
        return handleExceptionInternal(e, body, new HttpHeaders(), e.getErrorCode().getHttpStatus(), webRequest);
    }

    // 데이터 무결성 제약조건 핸들러
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            DataIntegrityViolationException e,
            WebRequest request
    ) {
        // 기본은 메시지
        String key = Optional.ofNullable(e.getMostSpecificCause())
                .map(Throwable::getMessage)
                .orElse("");

        // 원인 체인을 끝까지 훑어서 constraint name 우선 추출
        Throwable t = e;
        while (t != null) {
            if (t instanceof org.hibernate.exception.ConstraintViolationException cve) {
                String constraintName = cve.getConstraintName();
                if (constraintName != null && !constraintName.isBlank()) {
                    key = constraintName;
                    break;
                }
            }
            t = t.getCause();
        }

        for (DataIntegrityMapper mapper : dataIntegrityMappers) {
            if (mapper.supports(key)) {
                BaseCode code = mapper.errorCode();
                ApiResponse<Object> body = ApiResponse.onFailure(code, null);
                return handleExceptionInternal(e, body, new HttpHeaders(), code.getHttpStatus(), request);
            }
        }

        // 매핑 못한 경우 (공통 fallback)
        ApiResponse<Object> body = ApiResponse.onFailure(ErrorCode.DATA_INTEGRITY_VIOLATION, null);
        return handleExceptionInternal(
                e, body, new HttpHeaders(),
                ErrorCode.DATA_INTEGRITY_VIOLATION.getHttpStatus(),
                request
        );
    }


    // 모든 미처리 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnknownException(Exception e, WebRequest request) {
        log.error("Unhandled exception", e); // printStackTrace() 지양
        ApiResponse<Object> body = ApiResponse.onFailure(ErrorCode.INTERNAL_SERVER_ERROR, null);
        return handleExceptionInternal(e, body, new HttpHeaders(),
                ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus(), request);
    }


    @ExceptionHandler({InvalidFormatException.class})
    public ResponseEntity<ApiResponse<Object>> handleInvalidDateFormat(InvalidFormatException ex) {
        if (ex.getTargetType() == LocalDate.class) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.onFailure(ErrorCode.INVALID_DATE, null)
            );
        }
        return ResponseEntity.badRequest().body(
                ApiResponse.onFailure(ErrorCode.INVALID_FORMAT, null)
        );
    }

}

package app.tamingo.common.exception;

import app.tamingo.domain.schedule.exception.ScheduleDataIntegrityMapper;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExceptionAdviceDataIntegrityTest {

    @Test
    @DisplayName("DataIntegrityViolationException 발생 시 constraintName으로 매퍼가 선택되어 도메인 에러코드로 응답한다")
    void should_map_unique_constraint_to_domain_error_code() {
        // given
        DataIntegrityMapper scheduleMapper = new ScheduleDataIntegrityMapper();
        ExceptionAdvice advice = new ExceptionAdvice(Optional.of(List.of(scheduleMapper)));

        // Hibernate의 ConstraintViolationException(UNIQUE 제약) 시뮬레이션
        ConstraintViolationException hibernateEx =
                new ConstraintViolationException(
                        "duplicate key value violates unique constraint",
                        new SQLException("duplicate key"),
                        "uk_schedule_category_user_name"
                );

        // Spring이 감싸는 DataIntegrityViolationException 시뮬레이션
        DataIntegrityViolationException springEx =
                new DataIntegrityViolationException("data integrity violation", hibernateEx);

        WebRequest request = new ServletWebRequest(mock(HttpServletRequest.class));

        // when
        ResponseEntity<Object> response = advice.handleDataIntegrityViolation(springEx, request);

        // then
        assertThat(response.getStatusCode())
                .isEqualTo(ScheduleErrorCode.SCHEDULE_CATEGORY_DUPLICATED.getHttpStatus());
    }
}

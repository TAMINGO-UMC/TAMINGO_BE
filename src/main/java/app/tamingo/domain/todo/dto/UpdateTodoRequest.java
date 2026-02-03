package app.tamingo.domain.todo.dto;

import app.tamingo.domain.todo.enums.RepeatType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateTodoRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate targetDate,

        String placeName,
        String address,
        Double latitude,
        Double longitude,
        Integer duration,
        Long todoCategoryId,
        RepeatType repeatType,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate repeatEndDate,

        Long linkedScheduleId
) {
}

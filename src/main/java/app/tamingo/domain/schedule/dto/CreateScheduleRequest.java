package app.tamingo.domain.schedule.dto;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record CreateScheduleRequest(
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @NotNull(message = "날짜는 필수입니다.")
        LocalDate scheduleDate,

        @NotNull(message = "시작 시간은 필수입니다.")
        String startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        String endTime,

        //장소 정보
        String placeName,
        String address,
        Double latitude,
        Double longitude,

        Long scheduleCategoryId,
        String memo,
        RepeatType repeatType,
        LocalDate repeatEndDate,

        List<Long> linkedTodoIds,

        AiInferenceSource aiInferenceSource
) {
    public record AiInferenceSource(
            String aiSuggestedPlaceName,
            String aiSuggestedCategoryName
    ) {}
}

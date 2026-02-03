package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.schedule.entity.Schedule;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Builder
public record ScheduleDetailResponse(
        Long scheduleId,
        String title,
        LocalDate scheduleDate,
        String startTime,
        String endTime,
        String placeName,
        String address,
        Double latitude,
        Double longitude,
        String category,
        String repeatType,
        String repeatEndDate,
        String memo,
        List<ScheduleTodoResponse> linkedTodos,
        List<ScheduleTodoResponse> candidateTodos
) {
    public static ScheduleDetailResponse of(
            Schedule schedule,
            List<ScheduleTodoResponse> linkedTodos,
            List<ScheduleTodoResponse> candidateTodos
    ) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return ScheduleDetailResponse.builder()
                .scheduleId(schedule.getId())
                .title(schedule.getTitle())
                .scheduleDate(schedule.getStartTime().toLocalDate())
                .startTime(schedule.getStartTime().format(timeFormatter))
                .endTime(schedule.getEndTime().format(timeFormatter))
                .placeName(schedule.getPlaceName())
                .address(schedule.getAddress())
                .latitude(schedule.getLatitude())
                .longitude(schedule.getLongitude())
                .category(schedule.getScheduleCategory() != null ? schedule.getScheduleCategory().getName() : null)
                .repeatType(schedule.getRepeatType().name())
                .repeatEndDate(schedule.getRepeatEndDate() != null ? schedule.getRepeatEndDate().toString() : null)
                .memo(schedule.getMemo())
                .linkedTodos(linkedTodos)
                .candidateTodos(candidateTodos)
                .build();
    }
}

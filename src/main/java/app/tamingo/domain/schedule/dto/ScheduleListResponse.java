package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.schedule.entity.Schedule;

import java.time.LocalDateTime;

public record ScheduleListResponse(

        Long scheduleId,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String placeName,
        String category
) {
    public static ScheduleListResponse from(Schedule schedule){
        return new ScheduleListResponse(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getPlaceName(),
                schedule.getScheduleCategory() != null ? schedule.getScheduleCategory().getName() : null
        );
    }
}

package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.schedule.entity.Schedule;

public record ScheduleSummaryResponse(
        Long scheduleId,
        String title,
        String placeName
) {
    public static ScheduleSummaryResponse from(Schedule schedule){
        return new ScheduleSummaryResponse(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getPlaceName()
        );
    }

}

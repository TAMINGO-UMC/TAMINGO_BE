package app.tamingo.domain.home.dto;

import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;

import java.util.List;

public record TodoScheduleLinkGptRequest(
        List<ScheduleCandidate> candidates,
        List<CategoryCandidate> categories
) {
    public record ScheduleCandidate(
            Long scheduleId,
            String title,
            String timeRange
    ) {}

    public record CategoryCandidate(
            Long categoryId,
            String categoryName
    ) {}

    public static TodoScheduleLinkGptRequest from(
            List<Schedule> schedules,
            List<ScheduleCategory> categories
    ) {
        return new TodoScheduleLinkGptRequest(
                schedules.stream()
                        .map(s -> new ScheduleCandidate(
                                s.getId(),
                                s.getTitle(),
                                s.getStartTime().toLocalTime()
                                        + "~" +
                                        s.getEndTime().toLocalTime()
                        ))
                        .toList(),
                categories.stream()
                        .map(c -> new CategoryCandidate(
                                c.getId(),
                                c.getName()
                        ))
                        .toList()
        );
    }
}

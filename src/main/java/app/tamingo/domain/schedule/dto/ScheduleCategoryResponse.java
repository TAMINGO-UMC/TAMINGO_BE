package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.schedule.entity.ScheduleCategory;

public record ScheduleCategoryResponse(
        Long id, //아이디
        String name, //이름
        String colorCode //색상
) {
    public static ScheduleCategoryResponse from(ScheduleCategory c) {
        return new ScheduleCategoryResponse(c.getId(), c.getName(),c.getColorCode());
    }
}

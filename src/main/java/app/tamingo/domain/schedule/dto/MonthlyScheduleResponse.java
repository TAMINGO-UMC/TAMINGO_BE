package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.schedule.entity.ScheduleCategory;

import java.util.List;

public record MonthlyScheduleResponse(
        List<ScheduleListResponse> schedules,
        List<CategoryDto> categories
) {
    public static MonthlyScheduleResponse of(List<ScheduleListResponse> schedules, List<CategoryDto> categories) {
        return new MonthlyScheduleResponse(schedules, categories);
    }

    public record CategoryDto(
            Long categoryId,
            String name,
            String colorCode
    ) {
        public static CategoryDto from(ScheduleCategory category) {
            return new CategoryDto(
                    category.getId(),
                    category.getName(),
                    category.getColorCode()
            );
        }
    }
}

package app.tamingo.domain.todo.dto;

import app.tamingo.domain.schedule.dto.ScheduleSummaryResponse;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.enums.RepeatType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record TodoDetailResponse(
        Long todoId,
        String title,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate targetDate,

        String placeName,
        String address,
        Double latitude,
        Double longitude,
        Integer duration,

        String category,

        RepeatType repeatType,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate repeatEndDate,

        // 현재 연결된 일정 (없으면 null)
        ScheduleSummaryResponse linkedSchedule,

        // 연결 후보 리스트 (Nearby + Weekly 통합)
        List<ScheduleSummaryResponse> candidateSchedules,

        // 자주 가는 장소 추천 여부
        boolean isFavoriteRecommendation
) {
    public static TodoDetailResponse of(
            Todo todo,
            List<Schedule> nearbySchedules,
            List<Schedule> weeklySchedules,
            boolean isFavoriteRecommendation
    ) {
        // 연결된 일정 확인
        Long linkedScheduleId = (todo.getSchedule() != null) ? todo.getSchedule().getId() : null;

        // Nearby -> Weekly 순서
        Set<Schedule> mergedSchedules = new LinkedHashSet<>();

        // Nearby 추가 (이미 연결된 일정은 제외)
        for (Schedule s : nearbySchedules) {
            if (!s.getId().equals(linkedScheduleId)) {
                mergedSchedules.add(s);
            }
        }

        // Weekly 추가 (이미 연결된 일정은 제외)
        for (Schedule s : weeklySchedules) {
            if (!s.getId().equals(linkedScheduleId)) {
                mergedSchedules.add(s);
            }
        }

        return new TodoDetailResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getTargetDate(),
                todo.getPlaceName(),
                todo.getAddress(),
                todo.getLatitude(),
                todo.getLongitude(),
                todo.getDuration(),
                (todo.getTodoCategory() != null) ? todo.getTodoCategory().getName() : null,
                todo.getRepeatType(),
                todo.getRepeatEndDate(),

                // 연결된 일정 변환
                (todo.getSchedule() != null) ? ScheduleSummaryResponse.from(todo.getSchedule()) : null,

                // 후보 리스트 변환
                mergedSchedules.stream()
                        .map(ScheduleSummaryResponse::from)
                        .toList(),

                isFavoriteRecommendation
        );
    }
}

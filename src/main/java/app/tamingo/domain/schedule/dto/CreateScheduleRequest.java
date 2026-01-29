package app.tamingo.domain.schedule.dto;

import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record CreateScheduleRequest(
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

    public Schedule toEntity(User user, ScheduleCategory category){
        // String 시간을 localtime 으로 변환
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startLocalTime = LocalTime.parse(this.startTime, timeFormatter);
        LocalTime endLocalTime = LocalTime.parse(this.endTime, timeFormatter);

        // 종료 시간이 시작 시간보다 빠르면 에러
        if(endLocalTime.isBefore(startLocalTime)){
            throw new IllegalArgumentException("종료시간은 시작시간보다 빠를 수 없습니다.");
        }

        // 날짜+시간을 통해 LocalDateTime 생성
        LocalDateTime startDateTime = LocalDateTime.of(this.scheduleDate, startLocalTime);
        LocalDateTime endDateTime = LocalDateTime.of(this.scheduleDate, endLocalTime);

        return Schedule.of(
                user,
                category,
                this.title(),
                startDateTime,
                endDateTime,
                this.placeName(),
                this.address(),
                this.latitude(),
                this.longitude(),
                this.repeatType(),
                this.repeatEndDate(),
                this.memo()
        );

    }
}

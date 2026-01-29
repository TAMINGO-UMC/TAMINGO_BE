package app.tamingo.domain.home.converter;

import app.tamingo.domain.home.dto.DailyPlanResponse;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.schedule.entity.Schedule;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalTime;

public class DailyPlanItemConverter {

    public static DailyPlanResponse.ScheduleItem toScheduleItem(
            Schedule schedule,
            boolean isNextSchedule,
            LocalTime now
    ){
        int leftMinute = 0;

        // 남은 시간을 분으로 변환
        if(isNextSchedule && schedule.getStartTime().toLocalTime().isAfter(now)){
            leftMinute = (int) Duration
                    .between(now, schedule.getStartTime().toLocalTime())
                    .toMinutes();
        }

        return new DailyPlanResponse.ScheduleItem(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getStartTime().toLocalTime(),
                isNextSchedule,
                schedule.getPlaceName(),
                leftMinute
        );
    }

    public static DailyPlanResponse.GapRecommendItem toGapRecommendItem(
            SuggestionLearning suggestionLearning
   ){
        return new DailyPlanResponse.GapRecommendItem(
                suggestionLearning.getId(),
                suggestionLearning.getPlaceName(),
                suggestionLearning.getStartTime().toLocalTime(),
                suggestionLearning.getDuration(),
                suggestionLearning.getAiComment()
        );
    }
}

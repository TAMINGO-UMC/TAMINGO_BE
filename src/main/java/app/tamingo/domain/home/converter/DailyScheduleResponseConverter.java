package app.tamingo.domain.home.converter;

import app.tamingo.domain.home.dto.DailyScheduleResponse;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.CurrentStatus;
import app.tamingo.domain.home.redis.RealtimeSchedule;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DailyScheduleResponseConverter {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static DailyScheduleResponse.ScheduleStatusResponse toScheduleStatusResponse(
            CurrentStatus currentStatus,
            boolean isStarted,
            Integer leftOrDelayMinutes,
            LocalTime expectedDepartureTime,
            LocalTime expectedArrivalTime,
            LocalTime actualDepartureTime,
            LocalTime actualArrivalTime,
            Integer lateArrivalMinutes
    ) {
        return new DailyScheduleResponse.ScheduleStatusResponse(
                currentStatus,
                isStarted,
                leftOrDelayMinutes,
                expectedDepartureTime,
                expectedArrivalTime,
                lateArrivalMinutes
        );
    }

    public static DailyScheduleResponse.RecommendationResponse toRecommendationResponse(
            SuggestionLearning suggestionLearning
    ) {
        String detourMinutes = suggestionLearning.getDetourMinutes() != null
                ? String.valueOf(suggestionLearning.getDetourMinutes())
                : null;

        return new DailyScheduleResponse.RecommendationResponse(
                suggestionLearning.getId(),
                suggestionLearning.getTitle(),
                suggestionLearning.getPlaceName(),
                detourMinutes,
                suggestionLearning.getAiComment()
        );
    }

    private DailyScheduleResponse.ScheduleStatusResponse toResponse(RealtimeSchedule realtime) {
        LocalTime expectedDeparture = parseLocalTime(realtime.getExpectedDepartureTime());
        LocalTime expectedArrival = parseLocalTime(realtime.getExpectedArrivalTime());
        return new DailyScheduleResponse.ScheduleStatusResponse(
                realtime.getCurrentStatus(),
                realtime.isStarted(),
                realtime.getLeftOrDelayMinutes(),
                expectedDeparture,
                expectedArrival,
                realtime.getLateArrivalMinutes()
        );
    }

    private LocalTime parseLocalTime(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDateTime.parse(dateTime, ISO).toLocalTime();
    }

}

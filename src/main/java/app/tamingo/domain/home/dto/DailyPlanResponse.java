package app.tamingo.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class DailyPlanResponse {

    private LocalDate date;
    private List<DailyPlanItem> items;

    // 일정, 할일, 추천 일정을 포괄하는 클래스
    @Getter
    public static abstract class DailyPlanItem {
        private final DailyPlanItemType type;

        protected DailyPlanItem(DailyPlanItemType type) {
            this.type = type;
        }
    }

    @Getter
    public static class ScheduleItem extends DailyPlanItem {

        private final Long scheduleId;
        private final String title;
        private final LocalTime startTime;
        private final boolean isNextSchedule;
        private final String placeName;
        private final int leftMinute;
        private final int duration;

        public ScheduleItem(
                Long scheduleId,
                String title,
                LocalTime startTime,
                boolean isNextSchedule,
                String placeName,
                int leftMinute,
                int duration
        ) {
            super(DailyPlanItemType.SCHEDULE);
            this.scheduleId = scheduleId;
            this.title = title;
            this.startTime = startTime;
            this.isNextSchedule = isNextSchedule;
            this.placeName = placeName;
            this.leftMinute = leftMinute;
            this.duration = duration;
        }
    }


    // 틈새 추천 일정
    @Getter
    public static class GapRecommendItem extends DailyPlanItem {

        private Long suggestionId;
        private String location;
        private LocalTime time; // 시간
        private Integer requiredMinutes; // 소요시간
        private String title;
        private String message;

        public GapRecommendItem(
                Long suggestionId,
                String location,
                LocalTime time,
                Integer requiredMinutes,
                String title,
                String message
        ) {
            super(DailyPlanItemType.GAP_RECOMMEND);
            this.suggestionId = suggestionId;
            this.location = location;
            this.time = time;
            this.title = title;
            this.requiredMinutes = requiredMinutes;
            this.message = message;
        }
    }

    public enum DailyPlanItemType {
        SCHEDULE,
        GAP_RECOMMEND
    }
}

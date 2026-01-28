package app.tamingo.domain.home.entity;

import app.tamingo.domain.home.entity.enums.SuggestionPlanType;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "suggestion_learning",
        indexes = {
                @Index(name = "idx_suggestion_learning_user", columnList = "user_id"),
                @Index(name = "idx_suggestion_learning_schedule", columnList = "schedule_id"),
                @Index(name = "idx_suggestion_learning_type", columnList = "suggestion_type")
        }
)
public class SuggestionLearning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_type", nullable = false)
    private SuggestionType suggestionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type")
    private SuggestionPlanType planType;

    @Column(name = "accepted", nullable = false)
    private Boolean accepted = true;

    @Column(name = "place_name", nullable = false, length = 30)
    private String placeName;

    @Column(name = "latitude", nullable = true)
    private Double latitude;

    @Column(name = "longitude", nullable = true)
    private Double longitude;

    @Column(name = "ai_comment", nullable = false, length = 100)
    private String aiComment;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    // GAP_TIME일 때만
    @Column(name = "duration")
    private Integer duration;

    // ROUTE_DETOUR일 때만
    @Column(name = "detour_minutes")
    private Integer detourMinutes;

    // 연결된 할일 ID
    @Column(name = "linked_todo_id")
    private Long linkedTodoId;

    @Builder(builderMethodName = "internalBuilder")
    private SuggestionLearning(
            User user,
            Schedule schedule,
            SuggestionType suggestionType,
            SuggestionPlanType planType,
            boolean accepted,
            String placeName,
            double latitude,
            double longitude,
            String aiComment,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer duration,
            Integer detourMinutes,
            Long linkedTodoId) {
        this.user = user;
        this.schedule = schedule;
        this.suggestionType = suggestionType;
        this.planType = planType;
        this.accepted = accepted;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.aiComment = aiComment;
        this.startTime = startTime;
    }

    public static SuggestionLearning of(
            User user,
            Schedule schedule,
            SuggestionType suggestionType,
            SuggestionPlanType planType,
            String placeName,
            double latitude,
            double longitude,
            String aiComment,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer duration,
            Integer detourMinutes,
            Long linkedTodoId

    ){
        return SuggestionLearning.internalBuilder()
                .user(user)
                .schedule(schedule)
                .suggestionType(suggestionType)
                .planType(planType)
                .placeName(placeName)
                .latitude(latitude)
                .longitude(longitude)
                .aiComment(aiComment)
                .startTime(startTime)
                .endTime(endTime)
                .duration(duration)
                .detourMinutes(detourMinutes)
                .linkedTodoId(linkedTodoId)
                .build();
    }

    public void acceptSuggestion() {
        this.accepted = true;
    }

}

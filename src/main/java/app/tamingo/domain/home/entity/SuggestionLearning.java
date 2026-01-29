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

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggestion_type", nullable = false)
    private SuggestionType suggestionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type")
    private SuggestionPlanType planType;

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

    // AI 추천 카테고리 ID
    @Column(name = "suggested_category_id")
    private Long suggestedCategoryId;


    @Builder(builderMethodName = "internalBuilder")
    private SuggestionLearning(
            User user,
            String title,
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
            Long linkedTodoId,
            Long suggestedCategoryId) {
        this.user = user;
        this.title = title;
        this.schedule = schedule;
        this.suggestionType = suggestionType;
        this.planType = planType;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.aiComment = aiComment;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.detourMinutes = detourMinutes;
        this.linkedTodoId = linkedTodoId;
        this.suggestedCategoryId = suggestedCategoryId;
    }

    public static SuggestionLearning of(
            User user,
            String title,
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
            Long linkedTodoId,
            Long suggestedCategoryId

    ){
        return SuggestionLearning.internalBuilder()
                .user(user)
                .title(title)
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
                .suggestedCategoryId(suggestedCategoryId)
                .build();
    }

}

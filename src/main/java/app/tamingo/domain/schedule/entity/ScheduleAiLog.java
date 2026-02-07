package app.tamingo.domain.schedule.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedule_ai_log")
public class ScheduleAiLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 스케줄과 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // AI 제안 값
    @Column(name = "ai_suggested_category")
    private String aiSuggestedCategory;
    @Column(name = "ai_suggested_place")
    private String aiSuggestedPlace;

    // 사용자 최종 선택 값
    @Column(name = "user_selected_category")
    private String userSelectedCategory;
    @Column(name = "user_selected_place")
    private String userSelectedPlace;

    // 점수
    @Column(name = "score", nullable = false)
    private int score;

    @Builder(builderMethodName = "internalBuilder")
    private ScheduleAiLog(User user, Schedule schedule, String aiSuggestedCategory, String aiSuggestedPlace, String userSelectedCategory, String userSelectedPlace, int score) {
        this.user = user;
        this.schedule = schedule;
        this.aiSuggestedCategory = aiSuggestedCategory;
        this.aiSuggestedPlace = aiSuggestedPlace;
        this.userSelectedCategory = userSelectedCategory;
        this.userSelectedPlace = userSelectedPlace;
        this.score = score;
    }

    public static ScheduleAiLog of(
            User user, Schedule schedule,
            String aiSuggestedCategory, String aiSuggestedPlace,
            String userSelectedCategory, String userSelectedPlace, int score) {
        return ScheduleAiLog.internalBuilder()
                .user(user)
                .schedule(schedule)
                .aiSuggestedCategory(aiSuggestedCategory)
                .aiSuggestedPlace(aiSuggestedPlace)
                .userSelectedCategory(userSelectedCategory)
                .userSelectedPlace(userSelectedPlace)
                .score(score)
                .build();
    }

}

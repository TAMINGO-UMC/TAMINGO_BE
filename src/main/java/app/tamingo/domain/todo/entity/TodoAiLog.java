package app.tamingo.domain.todo.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "todo_ai_log")
public class TodoAiLog extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 투두와 연결
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    // AI 제안 값
    @Column(name = "ai_suggested_category")
    private String aiSuggestedCategory;
    @Column(name = "ai_suggested_place")
    private String aiSuggestedPlace;
    @Column(name = "ai_suggested_duration")
    private Integer aiSuggestedDuration;

    // 사용자 최종 선택 값
    @Column(name = "user_selected_category")
    private String userSelectedCategory;
    @Column(name = "user_selected_place")
    private String userSelectedPlace;
    @Column(name = "user_selected_duration")
    private Integer userSelectedDuration;


    // 점수
    @Column(name = "score")
    private int score;

    @Column(name = "is_log_updated", nullable = false)
    private boolean isLogUpdated = false;

    @Builder(builderMethodName = "internalBuilder")
    private TodoAiLog(
            User user, Todo todo, String aiSuggestedCategory, String aiSuggestedPlace,
            Integer aiSuggestedDuration, String userSelectedCategory, String userSelectedPlace,
            Integer userSelectedDuration, int score,boolean isLogUpdated) {
        this.user = user;
        this.todo = todo;
        this.aiSuggestedCategory = aiSuggestedCategory;
        this.aiSuggestedPlace = aiSuggestedPlace;
        this.aiSuggestedDuration = aiSuggestedDuration;
        this.userSelectedCategory = userSelectedCategory;
        this.userSelectedPlace = userSelectedPlace;
        this.userSelectedDuration = userSelectedDuration;
        this.score = score;
        this.isLogUpdated = isLogUpdated;
    }

    public static TodoAiLog of(
            User user, Todo todo, String aiSuggestedCategory, String aiSuggestedPlace,
            Integer aiSuggestedDuration, String userSelectedCategory,
            String userSelectedPlace,Integer userSelectedDuration, int score) {
        return TodoAiLog.internalBuilder()
                .user(user)
                .todo(todo)
                .aiSuggestedCategory(aiSuggestedCategory)
                .aiSuggestedPlace(aiSuggestedPlace)
                .aiSuggestedDuration(aiSuggestedDuration)
                .userSelectedCategory(userSelectedCategory)
                .userSelectedPlace(userSelectedPlace)
                .userSelectedDuration(userSelectedDuration)
                .score(score)
                .build();
    }

    public void updateUserSelection(
            String userSelectedCategory,
            String userSelectedPlace,
            Integer userSelectedDuration
    ) {
        // 1회만 수행
        if (this.isLogUpdated) {
            return;
        }

        this.userSelectedCategory = userSelectedCategory;
        this.userSelectedPlace = userSelectedPlace;
        this.userSelectedDuration = userSelectedDuration;

        // 점수 계산 (감점 방식 적용)
        this.score = calculateScore();

        // 업데이트 완료 처리
        this.isLogUpdated = true;
    }

    private int calculateScore() {
        int currentScore = 100; // 100점에서 시작

        // 카테고리 불일치 시 33점 감점
        if (!isMatch(this.aiSuggestedCategory, this.userSelectedCategory)) {
            currentScore -= 33;
        }

        // 장소 불일치 시 34점 감점
        if (!isMatch(this.aiSuggestedPlace, this.userSelectedPlace)) {
            currentScore -= 34;
        }

        // 소요시간 불일치 시 33점 감점
        if (!Objects.equals(this.aiSuggestedDuration, this.userSelectedDuration)) {
            currentScore -= 33;
        }

        // 음수 방지
        return Math.max(0, currentScore);
    }

    private boolean isMatch(String s1, String s2) {
        if (s1 == null || s2 == null) return false;
        return s1.trim().equalsIgnoreCase(s2.trim());
    }

}

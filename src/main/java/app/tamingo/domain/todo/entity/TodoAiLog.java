package app.tamingo.domain.todo.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    // 사용자 최종 선택 값
    @Column(name = "user_selected_category")
    private String userSelectedCategory;
    @Column(name = "user_selected_place")
    private String userSelectedPlace;

    // 점수
    @Column(name = "score")
    private int score;

    @Builder(builderMethodName = "internalBuilder")
    private TodoAiLog(User user, Todo todo, String aiSuggestedCategory, String aiSuggestedPlace, String userSelectedCategory, String userSelectedPlace, int score) {
        this.user = user;
        this.todo = todo;
        this.aiSuggestedCategory = aiSuggestedCategory;
        this.aiSuggestedPlace = aiSuggestedPlace;
        this.userSelectedCategory = userSelectedCategory;
        this.userSelectedPlace = userSelectedPlace;
        this.score = score;
    }

    public static TodoAiLog of(
            User user, Todo todo, String aiSuggestedCategory,
            String aiSuggestedPlace, String userSelectedCategory,
            String userSelectedPlace, int score) {
        return TodoAiLog.internalBuilder()
                .user(user)
                .todo(todo)
                .aiSuggestedCategory(aiSuggestedCategory)
                .aiSuggestedPlace(aiSuggestedPlace)
                .userSelectedCategory(userSelectedCategory)
                .userSelectedPlace(userSelectedPlace)
                .score(score)
                .build();
    }

}

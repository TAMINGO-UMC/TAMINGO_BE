package app.tamingo.domain.todo.entity;

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
public class TodoAiLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    // AI 제안 값
    @Column(name = "ai_suggested_category", nullable = false)
    private String aiSuggestedCategory;
    @Column(name = "ai_suggested_place", nullable = false)
    private String aiSuggestedPlace;

    // 사용자 최종 선택 값
    @Column(name = "user_selected_category", nullable = false)
    private String userSelectedCategory;
    @Column(name = "user_selected_place", nullable = false)
    private String userSelectedPlace;

    // 점수
    @Column(name = "score", nullable = false)
    private int score;

    @Builder
    public TodoAiLog(User user, Todo todo, String aiSuggestedCategory, String aiSuggestedPlace, String userSelectedCategory, String userSelectedPlace, int score) {
        this.user = user;
        this.todo = todo;
        this.aiSuggestedCategory = aiSuggestedCategory;
        this.aiSuggestedPlace = aiSuggestedPlace;
        this.userSelectedCategory = userSelectedCategory;
        this.userSelectedPlace = userSelectedPlace;
        this.score = score;
    }

}

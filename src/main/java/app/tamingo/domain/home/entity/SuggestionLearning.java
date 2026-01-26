package app.tamingo.domain.home.entity;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
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

    @Column(name = "accepted", nullable = false)
    private boolean accepted;

    @Column(name = "place_name", nullable = false, length = 30)
    private String placeName;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Column(name = "ai_comment", nullable = false, length = 100)
    private String aiComment;

    // GAP_TIME일 때만
    @Column(name = "duration")
    private Integer duration;

    // ROUTE_DETOUR일 때만
    @Column(name = "detour_minutes")
    private Integer detourMinutes;

}

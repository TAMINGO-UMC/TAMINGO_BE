package app.tamingo.domain.userlearning.entity;

import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_learning_summary",
        indexes = {
                @Index(name = "idx_learning_summary_user", columnList = "user_id")
        }
)
public class UserLearningSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "sample_count", nullable = false)
    private long sampleCount;

    @Column(name = "avg_accuracy_rate", nullable = false)
    private double avgAccuracyRate;

    @Column(name = "fvp_count", nullable = false)
    private int fvpCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

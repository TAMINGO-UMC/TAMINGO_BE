package app.tamingo.domain.userlearning.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_learning_summary",
        indexes = {
                @Index(name = "idx_learning_summary_user", columnList = "user_id")
        }
)
public class UserLearningSummary extends BaseEntity {

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

    @Builder(builderMethodName = "internalBuilder")
    private UserLearningSummary(
            User user,
            long sampleCount,
            double avgAccuracyRate,
            int fvpCount,
            LocalDateTime updatedAt) {
        this.user = user;
        this.sampleCount = sampleCount;
        this.avgAccuracyRate = avgAccuracyRate;
        this.fvpCount = fvpCount;
    }

    public static UserLearningSummary of(
            User user,
            long sampleCount,
            double avgAccuracyRate,
            int fvpCount
    ) {
        return UserLearningSummary.internalBuilder()
                .user(user)
                .sampleCount(sampleCount)
                .avgAccuracyRate(avgAccuracyRate)
                .fvpCount(fvpCount)
                .build();
    }
}

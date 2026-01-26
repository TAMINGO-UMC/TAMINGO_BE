package app.tamingo.domain.userlearning.entity;

import app.tamingo.domain.home.entity.TimeSlot;
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
        name = "user_learning_pattern",
        indexes = {
                @Index(name = "idx_learning_pattern_user", columnList = "user_id"),
                @Index(name = "idx_learning_pattern_slot_route", columnList = "time_slot, route_type")
        }
)
public class UserLearningPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 시간대 분류
    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false)
    private TimeSlot timeSlot;

    // 이동수단
    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false)
    private RouteType routeType;

    // 평균 도착시간 차이
    @Column(name = "avg_eta_diff", nullable = false)
    private int avgEtaDiff;

    // 학습한 오차로그 수
    @Column(name = "sample_count", nullable = false)
    private int sampleCount;

    @Column(name = "accuracy_rate", nullable = false)
    private double accuracyRate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

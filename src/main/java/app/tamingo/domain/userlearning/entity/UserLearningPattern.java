package app.tamingo.domain.userlearning.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.home.entity.enums.TimeSlot;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.enums.RouteType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_learning_pattern",
        indexes = {
                @Index(name = "idx_learning_pattern_user", columnList = "user_id"),
                @Index(name = "idx_learning_pattern_slot_route", columnList = "time_slot, route_type")
        }
)
public class UserLearningPattern extends BaseEntity {

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

    @Builder(builderMethodName = "internalBuilder")
    private UserLearningPattern(
            User user,
            TimeSlot timeSlot,
            RouteType routeType,
            int avgEtaDiff,
            int sampleCount,
            double accuracyRate,
            LocalDateTime updatedAt) {
        this.user = user;
        this.timeSlot = timeSlot;
        this.routeType = routeType;
        this.avgEtaDiff = avgEtaDiff;
        this.sampleCount = sampleCount;
        this.accuracyRate = accuracyRate;
    }

    public static UserLearningPattern of(
            User user,
            TimeSlot timeSlot,
            RouteType routeType,
            int avgEtaDiff,
            int sampleCount,
            double accuracyRate
    ) {
        return UserLearningPattern.internalBuilder()
                .user(user)
                .timeSlot(timeSlot)
                .routeType(routeType)
                .avgEtaDiff(avgEtaDiff)
                .sampleCount(sampleCount)
                .accuracyRate(accuracyRate)
                .build();
    }

    public void update(int avgEtaDiff, int sampleCount, double accuracyRate) {
        this.avgEtaDiff = avgEtaDiff;
        this.sampleCount+=sampleCount;
        this.accuracyRate = accuracyRate;
    }
}

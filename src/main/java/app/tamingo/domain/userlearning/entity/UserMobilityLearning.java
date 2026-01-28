package app.tamingo.domain.userlearning.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.home.entity.enums.ArrivedStatus;
import app.tamingo.domain.home.entity.enums.TimeSlot;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_mobility_learning",
        indexes = {
                @Index(name = "idx_user_mobility_time_slot", columnList = "time_slot")
        }
)
public class UserMobilityLearning extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false)
    private TimeSlot timeSlot;

    @Column(name = "predicted_time", nullable = false)
    private int predictedTime;

    @Column(name = "actual_time", nullable = false)
    private int actualTime;

    @Column(name = "usf_before", nullable = false)
    private double usfBefore;

    @Column(name = "usf_after", nullable = false)
    private double usfAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "arrived_status", nullable = false)
    private ArrivedStatus arrivedStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder(builderMethodName = "internalBuilder")
    private UserMobilityLearning(
            TimeSlot timeSlot,
            int predictedTime,
            int actualTime,
            double usfBefore,
            double usfAfter,
            ArrivedStatus arrivedStatus,
            User user) {
        this.timeSlot = timeSlot;
        this.predictedTime = predictedTime;
        this.actualTime = actualTime;
        this.usfBefore = usfBefore;
        this.usfAfter = usfAfter;
        this.arrivedStatus = arrivedStatus;
        this.user = user;
    }

    public static UserMobilityLearning of(
            TimeSlot timeSlot,
            int predictedTime,
            int actualTime,
            double usfBefore,
            double usfAfter,
            ArrivedStatus arrivedStatus,
            User user
    ) {
        return UserMobilityLearning.internalBuilder()
                .timeSlot(timeSlot)
                .predictedTime(predictedTime)
                .actualTime(actualTime)
                .usfBefore(usfBefore)
                .usfAfter(usfAfter)
                .arrivedStatus(arrivedStatus)
                .user(user)
                .build();
    }
}

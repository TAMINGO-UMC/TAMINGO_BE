package app.tamingo.domain.userlearning.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.home.entity.ArrivedStatus;
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
}

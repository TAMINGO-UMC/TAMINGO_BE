package app.tamingo.domain.userlearning.entity;

import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "departure_alarm",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_departure_alarm_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_departure_alarm_user", columnList = "user_id")
        }
)
public class DepartureAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    // 적용된 usf
    @Column(name = "usf_applied", nullable = false)
    private double usfApplied;

    // 출발 알림 시간
    @Column(name = "notify_at_minutes", nullable = false)
    private int notifyAtMinutes;

    @Builder(builderMethodName = "internalBuilder")
    private DepartureAlarm(
            User user,
            double usfApplied,
            int notifyAtMinutes
    ) {
        this.user = user;
        this.usfApplied = usfApplied;
        this.notifyAtMinutes = notifyAtMinutes;
    }

    public static DepartureAlarm of(
            User user,
            double usfApplied,
            int notifyAtMinutes
    ) {
        return DepartureAlarm.internalBuilder()
                .user(user)
                .usfApplied(usfApplied)
                .notifyAtMinutes(notifyAtMinutes)
                .build();
    }



    public void updateUsfAndNotifyAtMinutes(double usfApplied, int notifyAtMinutes) {
        this.usfApplied = usfApplied;
        this.notifyAtMinutes = notifyAtMinutes;
    }
}

package app.tamingo.domain.userlearning.entity;

import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "departure_alarm",
        indexes = {
                @Index(name = "idx_departure_alarm_user", columnList = "user_id")
        }
)
public class DepartureAlarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 지도 API ETA
    @Column(name = "base_eta")
    private Integer baseEta;

    // 적용된 usf
    @Column(name = "usf_applied", nullable = false)
    private double usfApplied;

    // 실제로 사용된 ETA
    @Column(name = "final_eta")
    private Integer finalEta;

    // 출발 알림 시간
    @Column(name = "notify_at", nullable = false)
    private Time notifyAt;

    @Column(name = "arrival_buffer_minutes", nullable = false)
    private int arrivalBufferMinutes;

    @Column(name = "is_applied", nullable = false)
    private boolean isApplied = false;

    @Builder(builderMethodName = "internalBuilder")
    private DepartureAlarm(
            User user,
            Integer baseEta,
            double usfApplied,
            Integer finalEta,
            Time notifyAt,
            int arrivalBufferMinutes,
            boolean isApplied
    ) {
        this.user = user;
        this.baseEta = baseEta;
        this.usfApplied = usfApplied;
        this.finalEta = finalEta;
        this.notifyAt = notifyAt;
        this.arrivalBufferMinutes = arrivalBufferMinutes;
        this.isApplied = isApplied;
    }

    public static DepartureAlarm of(
            User user,
            Integer baseEta,
            double usfApplied,
            Integer finalEta,
            Time notifyAt,
            int arrivalBufferMinutes
    ) {
        return DepartureAlarm.internalBuilder()
                .user(user)
                .baseEta(baseEta)
                .usfApplied(usfApplied)
                .finalEta(finalEta)
                .notifyAt(notifyAt)
                .arrivalBufferMinutes(arrivalBufferMinutes)
                .build();
    }

    public void markAsApplied() {
        this.isApplied = true;
    }
}

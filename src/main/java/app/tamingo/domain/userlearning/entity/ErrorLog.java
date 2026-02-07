package app.tamingo.domain.userlearning.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.home.entity.enums.ArrivedStatus;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "error_log",
        indexes = {
                @Index(name = "idx_error_log_user", columnList = "user_id")
        }
)
public class ErrorLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "departure_place", nullable = false, length = 20)
    private String departurePlace;

    @Column(name = "arrival_place", nullable = false, length = 20)
    private String arrivalPlace;

    @Column(name = "expected_duration", nullable = false)
    private int expectedDuration;

    @Column(name = "total_duration", nullable = false)
    private int totalDuration;

    @Column(name = "error_minutes", nullable = false)
    private int errorMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ArrivedStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(builderMethodName = "internalBuilder")
    private ErrorLog(
            String departurePlace,
            String arrivalPlace,
            int expectedDuration,
            int totalDuration,
            int errorMinutes,
            ArrivedStatus status,
            User user
    ) {
        this.departurePlace = departurePlace;
        this.arrivalPlace = arrivalPlace;
        this.expectedDuration = expectedDuration;
        this.totalDuration = totalDuration;
        this.errorMinutes = errorMinutes;
        this.status = status;
        this.user = user;
    }

    public static ErrorLog of(
            String departurePlace,
            String arrivalPlace,
            int expectedDuration,
            int totalDuration,
            int errorMinutes,
            ArrivedStatus status,
            User user
    ) {
        return ErrorLog.internalBuilder()
                .departurePlace(departurePlace)
                .arrivalPlace(arrivalPlace)
                .expectedDuration(expectedDuration)
                .totalDuration(totalDuration)
                .errorMinutes(errorMinutes)
                .status(status)
                .user(user)
                .build();
    }
}

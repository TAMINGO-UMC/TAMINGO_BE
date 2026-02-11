package app.tamingo.domain.notificationsetting.entity;


import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity(name = "NotificationSetting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="notification_setting")
public class NotificationSetting extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "depart_alert_enabled", nullable = false)
    private boolean departureAlertEnabled;

    @Column(name = "depart_alert_minutes", nullable = false)
    private int departureLeadMinutes;

    @Column(name = "realtime_transit_enabled", nullable = false)
    private boolean realtimeTransitEnabled;

    @Column(name = "todo_recommend_enabled", nullable = false)
    private boolean todoProposalEnabled;

    @Column(name = "location_move_check_enabled", nullable = false)
    private boolean locationMoveCheckEnabled;


    @Builder
    private NotificationSetting(User user, boolean departureAlertEnabled, int departureLeadMinutes,
                                boolean realtimeTransitEnabled, boolean todoProposalEnabled, boolean locationMoveCheckEnabled) {
        this.user = user;
        this.departureAlertEnabled = departureAlertEnabled;
        this.departureLeadMinutes = departureLeadMinutes;
        this.realtimeTransitEnabled = realtimeTransitEnabled;
        this.todoProposalEnabled = todoProposalEnabled;
        this.locationMoveCheckEnabled = locationMoveCheckEnabled;
    }

    public static NotificationSetting of(User user) {
        return NotificationSetting.builder()
                .user(user)
                .departureAlertEnabled(true)
                .departureLeadMinutes(30)
                .realtimeTransitEnabled(true)
                .todoProposalEnabled(true)
                .locationMoveCheckEnabled(false)
                .build();
    }

    public void update(
            boolean departureAlertEnabled, int departureLeadMinutes,
            boolean realtimeTransitEnabled, boolean todoProposalEnabled,
            boolean locationMoveCheckEnabled
    ) {
        this.departureAlertEnabled = departureAlertEnabled;
        this.departureLeadMinutes = departureLeadMinutes;
        this.realtimeTransitEnabled = realtimeTransitEnabled;
        this.todoProposalEnabled = todoProposalEnabled;
        this.locationMoveCheckEnabled = locationMoveCheckEnabled;
    }

    // 온보딩 전용 메서드
    public void updateDepartureAlert(boolean enabled, int leadMinutes) {
        this.departureAlertEnabled = enabled;
        this.departureLeadMinutes = leadMinutes;
    }
}

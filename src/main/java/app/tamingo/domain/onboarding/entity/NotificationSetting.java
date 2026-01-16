package app.tamingo.domain.onboarding.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseEntity {

    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "depart_alert_enabled", nullable = false)
    private boolean departAlertEnabled;

    @Column(name = "depart_alert_minutes", nullable = false)
    private short departAlertMinutes;

    public static NotificationSetting create(User user, boolean enabled, int minutes) {
        NotificationSetting s = new NotificationSetting();
        s.user = user;
        s.userId = user.getId();
        s.departAlertEnabled = enabled;
        s.departAlertMinutes = (short) minutes;
        return s;
    }

    public void update(boolean enabled, int minutes) {
        this.departAlertEnabled = enabled;
        this.departAlertMinutes = (short) minutes;
    }
}
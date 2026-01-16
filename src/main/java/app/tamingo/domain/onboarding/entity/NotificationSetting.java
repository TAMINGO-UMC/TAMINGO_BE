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
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "depart_alert_enabled", nullable = false)
    private boolean departAlertEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "depart_alert_minutes", nullable = false, length = 10)
    private AlertMinute departAlertMinute;

    public static NotificationSetting create(User user, boolean enabled, AlertMinute minute) {
        NotificationSetting s = new NotificationSetting();
        s.user = user;
        s.userId = user.getId();
        s.departAlertEnabled = enabled;
        s.departAlertMinute = minute;
        return s;
    }

    public void update(boolean enabled, AlertMinute minute) {
        this.departAlertEnabled = enabled;
        this.departAlertMinute = minute;
    }
}
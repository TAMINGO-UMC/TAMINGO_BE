package app.tamingo.domain.onboarding.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.notificationsetting.entity.AlertMinute;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "NotificationOnSetting")
@Table(name = "notification_on_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "depart_alert_enabled", nullable = false)
    private boolean departAlertEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "depart_alert_minutes", nullable = false, length = 10)
    private AlertMinute departAlertMinute;

    @Builder(builderMethodName = "internalBuilder")
    private NotificationSetting(
            User user,
            boolean departAlertEnabled,
            AlertMinute departAlertMinute
    ) {
        this.user = user;
        this.departAlertEnabled = departAlertEnabled;
        this.departAlertMinute = departAlertMinute;
    }

    public static NotificationSetting of(User user, boolean enabled, AlertMinute minute) {
        return NotificationSetting.internalBuilder()
                .user(user)
                .departAlertEnabled(enabled)
                .departAlertMinute(minute)
                .build();
    }

    public void update(boolean enabled, AlertMinute minute) {
        this.departAlertEnabled = enabled;
        this.departAlertMinute = minute;
    }
}
package app.tamingo.domain.notificationsetting.repository;

import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
}

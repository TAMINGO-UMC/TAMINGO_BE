package app.tamingo.domain.onboarding.repository;

import app.tamingo.domain.onboarding.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
}
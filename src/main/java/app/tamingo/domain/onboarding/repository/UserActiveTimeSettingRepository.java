package app.tamingo.domain.onboarding.repository;

import app.tamingo.domain.onboarding.entity.UserActiveTimeSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActiveTimeSettingRepository extends JpaRepository<UserActiveTimeSetting, Long> {
}
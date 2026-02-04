package app.tamingo.domain.userlearning.repository;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalSettingRepository extends JpaRepository<PersonalSetting, Long> {
    PersonalSetting findByUser(User user);
}

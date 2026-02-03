package app.tamingo.domain.userlearning.repository;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.DepartureAlarm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartureAlarmRepository extends JpaRepository<DepartureAlarm, Long> {
    Optional<DepartureAlarm> findByUser(User user);
}

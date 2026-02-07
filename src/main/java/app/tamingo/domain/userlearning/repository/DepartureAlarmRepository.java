package app.tamingo.domain.userlearning.repository;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.DepartureAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartureAlarmRepository extends JpaRepository<DepartureAlarm, Long> {
    Optional<DepartureAlarm> findByUser(User user);
    Optional<DepartureAlarm> findByUserId(Long userId);
}

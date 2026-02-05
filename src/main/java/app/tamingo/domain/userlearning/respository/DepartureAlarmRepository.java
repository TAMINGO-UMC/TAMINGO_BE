package app.tamingo.domain.userlearning.respository;

import app.tamingo.domain.userlearning.entity.DepartureAlarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartureAlarmRepository extends JpaRepository<DepartureAlarm, Long> {
    Optional<DepartureAlarm> findByUserId(Long userId);
}

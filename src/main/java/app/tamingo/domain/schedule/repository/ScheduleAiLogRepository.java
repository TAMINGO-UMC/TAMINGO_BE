package app.tamingo.domain.schedule.repository;

import app.tamingo.domain.schedule.entity.ScheduleAiLog;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleAiLogRepository extends JpaRepository<ScheduleAiLog, Long> {
    void deleteByUser(User user);
}

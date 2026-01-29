package app.tamingo.domain.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.tamingo.domain.schedule.entity.ScheduleAiLog;

public interface ScheduleAiLogRepository extends JpaRepository<ScheduleAiLog, Long> {
}

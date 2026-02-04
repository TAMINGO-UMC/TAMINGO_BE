package app.tamingo.domain.schedule.repository;

import app.tamingo.domain.schedule.entity.ScheduleResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScheduleResultRepository extends JpaRepository<ScheduleResult, Long> {

    Optional<ScheduleResult> findByScheduleId(Long scheduleId);
    boolean existsByScheduleId(Long scheduleId);
    List<ScheduleResult> findAllByScheduleIdIn(Collection<Long> scheduleIds);
}
package app.tamingo.domain.home.repository;

import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScheduleStartSnapshotRepository extends JpaRepository<ScheduleStartSnapshot, Long> {
    boolean existsBySchedule(Schedule schedule);

    Optional<ScheduleStartSnapshot> findBySchedule(Schedule schedule);
}

package app.tamingo.domain.home.repository;

import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ScheduleStartSnapshotRepository extends JpaRepository<ScheduleStartSnapshot, Long> {
    @Query("SELECT s FROM ScheduleStartSnapshot s " +
            "JOIN FETCH s.schedule sch " +
            "JOIN FETCH sch.user " +
            "WHERE s.isReserved = false")
    List<ScheduleStartSnapshot> findAllByIsReservedFalse();
    boolean existsBySchedule(Schedule schedule);

    Optional<ScheduleStartSnapshot> findBySchedule(Schedule schedule);
}

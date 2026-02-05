package app.tamingo.domain.schedule.repository;

import app.tamingo.domain.schedule.entity.ScheduleAiLog;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleAiLogRepository extends JpaRepository<ScheduleAiLog, Long> {
    void deleteByUser(User user);

    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(s.score), 0) FROM ScheduleAiLog s WHERE s.user = :user")
    int sumScoreByUser(@Param("user") User user);
}

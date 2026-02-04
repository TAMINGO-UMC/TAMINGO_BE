package app.tamingo.domain.userlearning.repository;

import app.tamingo.domain.userlearning.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    @Query(value = "select * from error_log where user_id = :userId order by created_at desc limit 10",
            nativeQuery = true)
    java.util.List<ErrorLog> findLatest10ByUser(@Param("userId") Long userId);
}

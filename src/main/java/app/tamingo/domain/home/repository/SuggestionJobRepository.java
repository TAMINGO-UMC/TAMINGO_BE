package app.tamingo.domain.home.repository;

import app.tamingo.domain.home.job.SuggestionJob;
import app.tamingo.domain.home.job.SuggestionJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SuggestionJobRepository extends JpaRepository<SuggestionJob, Long> {

    @Query(
            value = """
                    SELECT *
                    FROM suggestion_job
                    WHERE status = 'PENDING'
                      AND scheduled_at <= :now
                    ORDER BY scheduled_at ASC
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    Optional<SuggestionJob> findNextJobForUpdate(@Param("now") LocalDateTime now);

    long countByStatus(SuggestionJobStatus status);
}

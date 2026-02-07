package app.tamingo.domain.home.job;

import app.tamingo.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "suggestion_job",
        indexes = {
                @Index(name = "idx_suggestion_job_status_time", columnList = "status, scheduled_at"),
                @Index(name = "idx_suggestion_job_user", columnList = "user_id")
        }
)
public class SuggestionJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private SuggestionJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SuggestionJobStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Builder(builderMethodName = "internalBuilder")
    private SuggestionJob(
            Long userId,
            SuggestionJobType jobType,
            SuggestionJobStatus status,
            LocalDateTime scheduledAt,
            int retryCount
    ) {
        this.userId = userId;
        this.jobType = jobType;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.retryCount = retryCount;
    }

    public static SuggestionJob of(Long userId, SuggestionJobType jobType, LocalDateTime scheduledAt) {
        return SuggestionJob.internalBuilder()
                .userId(userId)
                .jobType(jobType)
                .status(SuggestionJobStatus.PENDING)
                .scheduledAt(scheduledAt)
                .retryCount(0)
                .build();
    }

    public void markRunning(LocalDateTime startedAt) {
        this.status = SuggestionJobStatus.RUNNING;
        this.startedAt = startedAt;
    }

    public void markDone(LocalDateTime finishedAt) {
        this.status = SuggestionJobStatus.DONE;
        this.finishedAt = finishedAt;
        this.lastError = null;
    }

    public void markFailed(LocalDateTime finishedAt, String lastError) {
        this.status = SuggestionJobStatus.FAILED;
        this.finishedAt = finishedAt;
        this.lastError = lastError;
        this.retryCount += 1;
    }
}

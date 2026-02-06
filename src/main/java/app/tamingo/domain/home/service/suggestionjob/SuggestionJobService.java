package app.tamingo.domain.home.service.suggestionjob;

import app.tamingo.domain.home.job.SuggestionJob;
import app.tamingo.domain.home.job.SuggestionJobStatus;
import app.tamingo.domain.home.job.SuggestionJobType;
import app.tamingo.domain.home.repository.SuggestionJobRepository;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuggestionJobService {

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final SuggestionJobRepository suggestionJobRepository;

    @Transactional
    public void enqueueDailyJobs() {
        List<Long> userIds = userRepository.findAllUserIds();
        LocalDate today = LocalDate.now(TARGET_ZONE);
        LocalDateTime gapTime = today.atStartOfDay();
        LocalDateTime detourTime = gapTime.plusMinutes(1);

        for (Long userId : userIds) {
            suggestionJobRepository.save(SuggestionJob.of(userId, SuggestionJobType.GAP, gapTime));
            suggestionJobRepository.save(SuggestionJob.of(userId, SuggestionJobType.DETOUR, detourTime));
        }

        log.info("[HOME][JOB] enqueue daily suggestion jobs. users={}", userIds.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<SuggestionJob> claimNextJob() {
        Optional<SuggestionJob> job = suggestionJobRepository.findNextJobForUpdate(LocalDateTime.now(TARGET_ZONE));
        job.ifPresent(value -> {
            value.markRunning(LocalDateTime.now(TARGET_ZONE));
            suggestionJobRepository.save(value);
        });
        return job;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDone(Long jobId) {
        suggestionJobRepository.findById(jobId).ifPresent(job -> {
            job.markDone(LocalDateTime.now(TARGET_ZONE));
            suggestionJobRepository.save(job);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long jobId, String lastError) {
        suggestionJobRepository.findById(jobId).ifPresent(job -> {
            job.markFailed(LocalDateTime.now(TARGET_ZONE), lastError);
            suggestionJobRepository.save(job);
        });
    }

    public long countPending() {
        return suggestionJobRepository.countByStatus(SuggestionJobStatus.PENDING);
    }
}

package app.tamingo.domain.home.scheduler;

import app.tamingo.domain.home.job.SuggestionJob;
import app.tamingo.domain.home.job.SuggestionJobType;
import app.tamingo.domain.home.service.gapsuggestion.GapSuggestionBatchService;
import app.tamingo.domain.home.service.routedetour.RouteDetourSuggestionBatchService;
import app.tamingo.domain.home.service.suggestionjob.SuggestionJobService;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuggestionJobWorker {

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final SuggestionJobService suggestionJobService;
    private final UserRepository userRepository;
    private final GapSuggestionBatchService gapSuggestionBatchService;
    private final RouteDetourSuggestionBatchService routeDetourSuggestionBatchService;

    @Scheduled(fixedDelay = 1000, zone = "Asia/Seoul")
    public void processNext() {
        Optional<SuggestionJob> claimed = suggestionJobService.claimNextJob();
        if (claimed.isEmpty()) {
            return;
        }

        SuggestionJob job = claimed.get();
        try {
            User user = userRepository.findById(job.getUserId()).orElse(null);
            if (user == null) {
                suggestionJobService.markFailed(job.getId(), "user_not_found");
                return;
            }

            if (job.getJobType() == SuggestionJobType.GAP) {
                gapSuggestionBatchService.runForUser(user, LocalDate.now(TARGET_ZONE));
                log.info("[HOME][JOB][GAP] done userId={}, jobId={}", user.getId(), job.getId());
            } else if (job.getJobType() == SuggestionJobType.DETOUR) {
                routeDetourSuggestionBatchService.runForUser(user, LocalDate.now(TARGET_ZONE));
                log.info("[HOME][JOB][DETOUR] done userId={}, jobId={}", user.getId(), job.getId());
            } else {
                log.warn("[HOME][JOB] unknown job type. jobId={}, type={}", job.getId(), job.getJobType());
            }

            suggestionJobService.markDone(job.getId());
        } catch (Exception ex) {
            log.warn("[HOME][JOB] failed jobId={}, type={}", job.getId(), job.getJobType(), ex);
            suggestionJobService.markFailed(job.getId(), ex.getMessage());
        }
    }
}

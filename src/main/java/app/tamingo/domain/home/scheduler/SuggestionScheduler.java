package app.tamingo.domain.home.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuggestionScheduler {

    private final HomeSuggestionAsyncRunner homeSuggestionAsyncRunner;

    /**
     * 매일 자정에 모든 사용자의 틈새시간 추천 / 동선 연계 추천 배치 작업 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void generateDailyGapSuggestions() {
        log.info("[HOME][SUGGESTION] 배치 시작");
        try {
            var gapFuture = homeSuggestionAsyncRunner.runGapSuggestions();
            var detourFuture = homeSuggestionAsyncRunner.runDetourSuggestions();
            java.util.concurrent.CompletableFuture.allOf(gapFuture, detourFuture).join();
            log.info("[HOME][SUGGESTION] 배치 완료");
        } catch (Exception e) {
            log.error("[HOME][SUGGESTION] 배치 실패", e);
        }
    }
}

package app.tamingo.domain.home.scheduler;

import app.tamingo.domain.home.service.gapsuggestion.GapSuggestionBatchService;
import app.tamingo.domain.home.service.routedetour.RouteDetourSuggestionBatchService;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HomeSuggestionAsyncRunner {

    private final GapSuggestionBatchService gapSuggestionBatchService;
    private final RouteDetourSuggestionBatchService routeDetourSuggestionBatchService;

    // 별도의 스레드풀에서 실행
    @Async("homeSuggestionExecutor")
    public CompletableFuture<Void> runGapSuggestions() {
        long startedAt = System.currentTimeMillis();
        log.info("[HOME][GAP] 배치 시작");
        gapSuggestionBatchService.runForAllUsers();
        log.info("[HOME][GAP] 배치 완료 ({} ms)", System.currentTimeMillis() - startedAt);
        return CompletableFuture.completedFuture(null);
    }

    @Async("homeSuggestionExecutor")
    public CompletableFuture<Void> runDetourSuggestions() {
        long startedAt = System.currentTimeMillis();
        log.info("[HOME][DETOUR] 배치 시작");
        routeDetourSuggestionBatchService.runForAllUsers();
        log.info("[HOME][DETOUR] 배치 완료 ({} ms)", System.currentTimeMillis() - startedAt);
        return CompletableFuture.completedFuture(null);
    }
}

package app.tamingo.domain.home.scheduler;

import app.tamingo.domain.home.service.gapsuggestion.GapSuggestionBatchService;
import app.tamingo.domain.home.service.routedetour.RouteDetourSuggestionBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuggestionScheduler {

    private final GapSuggestionBatchService gapSuggestionBatchService;
    private final RouteDetourSuggestionBatchService routeDetourSuggestionBatchService;

    /**
     * 매일 자정에 모든 사용자의 틈새시간 추천 / 동선 연계 추천 배치 작업 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void generateDailyGapSuggestions() {
        gapSuggestionBatchService.runForAllUsers();
        log.info("[HOME][GAP] 모든 사용자 틈새시간 추천 배치 작업 완료");
        // TODO : 알림 전송 로직 - 틈새시간만 해당
        routeDetourSuggestionBatchService.runForAllUsers();
        log.info("[HOME][DETOUR] 모든 사용자 동선연계 추천 배치 작업 완료");
    }
}

package app.tamingo.domain.home.scheduler;

import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import app.tamingo.domain.home.service.realtime.ScheduleInitQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleInitScheduler {

    // 한국시간으로 통일
    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    // 한번에 처리할 최대 일정 수, 데모 버전이므로 20으로 설정 - 서버 과부하 방지
    private static final int BATCH_LIMIT = 20;

    private final ScheduleInitQueueService scheduleInitQueueService;
    private final RealTimeScheduleService realTimeScheduleService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void processScheduleInitQueue() {
        LocalDateTime now = LocalDateTime.now(TARGET_ZONE);
        // 다가오는 일정을 가져옴
        List<Long> scheduleIds = scheduleInitQueueService.fetchDue(now, BATCH_LIMIT);
        for (Long scheduleId : scheduleIds) {
            try {
                // 다가오는 일정을 실시간 추적하도록 하는 메서드 실행
                realTimeScheduleService.initializeRealtimeFromSnapshot(scheduleId, now);
            } catch (Exception ex) {
                log.warn("[HOME][REALTIME_INIT] 초기화 실패 scheduleId={}", scheduleId, ex);
            }
        }
    }
}

package app.tamingo.domain.home.scheduler;

import app.tamingo.domain.home.service.gps.GpsBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleGPSScheduler {

    private final GpsBatchService gpsBatchService;

    /**
     * 오늘 일정 기준 지오펜싱/사후확인 처리
     *      *  -> REDIS ZEST로 변경해서 일정 생성 시에 붙이는 방식으로 수정 필요
     *      *  -> 구현 후 해당 스케줄러는 삭제
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void processGpsBatch() {
        gpsBatchService.processTodaySchedules();
        gpsBatchService.requestLocationUpdates();
    }
}

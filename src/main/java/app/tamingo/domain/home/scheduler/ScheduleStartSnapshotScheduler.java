package app.tamingo.domain.home.scheduler;

import app.tamingo.domain.home.service.startplace.ScheduleStartSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduleStartSnapshotScheduler {

    private final ScheduleStartSnapshotService scheduleStartSnapshotService;

    /**
     * 모든 일정의 시작 1시간 전에 출발 스냅샷 저장하는 스케줄러
     *  -> REDIS ZEST로 변경해서 일정 생성 시에 붙이는 방식으로 수정 필요
     *  -> 구현 후 해당 스케줄러는 삭제
     */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void captureStartSnapshots() {
        // 출발 스냅샷 저장
        // TODO: Silent GPS 전송 알림
        scheduleStartSnapshotService.createSnapshotsForStartMinusOneHour();
    }
}

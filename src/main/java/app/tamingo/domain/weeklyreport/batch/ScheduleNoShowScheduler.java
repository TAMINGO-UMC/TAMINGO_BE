package app.tamingo.domain.weeklyreport.batch;

import app.tamingo.domain.schedule.service.ScheduleNoShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ScheduleNoShowScheduler {

    private final ScheduleNoShowService scheduleNoShowService;

    /**
     * 10분마다 NO_SHOW 확정 처리
     * - cutoff 지난 일정 중 도착 확정이 없는(PENDING/결과없음) 일정들을 NO_SHOW로 확정
     */
    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    public void confirmNoShows() {
        scheduleNoShowService.confirmNoShows(LocalDateTime.now());
    }
}

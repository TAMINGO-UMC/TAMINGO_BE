package app.tamingo.domain.home.scheduler;

import app.tamingo.domain.home.service.realtime.ScheduleInitQueueService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
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
public class ScheduleInitEnqueueScheduler {

    // 한국시간으로 통일
    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");
    private final ScheduleInitQueueService scheduleInitQueueService;
    private final ScheduleRepository scheduleRepository;

    // 일정 시작 20분 전 초기화 큐 등록
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void enqueueScheduleInit() {
        LocalDateTime now = LocalDateTime.now(TARGET_ZONE);
        LocalDateTime windowStart = now.plusMinutes(20);
        LocalDateTime windowEnd = windowStart.plusMinutes(1);

        List<Schedule> schedules = scheduleRepository
                .findAllStartTimeBetween(windowStart, windowEnd);

        if (schedules.isEmpty()) {
            return;
        }

        for (Schedule schedule : schedules) {
            scheduleInitQueueService.scheduleInit(
                    schedule.getId(),
                    schedule.getStartTime().minusMinutes(20)
            );
        }

        log.info("[HOME][REALTIME_INIT] enqueue {} schedules", schedules.size());
    }

}

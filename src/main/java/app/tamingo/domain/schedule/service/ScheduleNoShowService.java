package app.tamingo.domain.schedule.service;

import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleResult;
import app.tamingo.domain.schedule.enums.ScheduleResultStatus;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.schedule.repository.ScheduleResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleNoShowService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleResultRepository scheduleResultRepository;

    // 정책값: 시작 후 60분이 지나도록 도착 확정이 없으면 NO_SHOW 확정
    private static final long NO_SHOW_CUTOFF_MINUTES = 60;

    @Transactional
    public void confirmNoShows(LocalDateTime now) {
        LocalDateTime cutoffTime = now.minusMinutes(NO_SHOW_CUTOFF_MINUTES);

        List<Schedule> candidates = scheduleRepository.findNoShowCandidates(cutoffTime);

        for (Schedule schedule : candidates) {
            ScheduleResult result = scheduleResultRepository.findByScheduleId(schedule.getId())
                    .orElseGet(() -> ScheduleResult.of(
                            schedule,
                            ScheduleResultStatus.PENDING,
                            false,              // navigationUsed
                            null,               // arrivedAt
                            null,               // lateMinutes
                            0,                  // punctualityScore
                            now                 // evaluatedAt
                    ));

            // 이미 PENDING인 것만 잡혀오지만 방어적으로 체크
            if (result.getStatus() == ScheduleResultStatus.PENDING) {
                result.confirmNoShow(now);
                scheduleResultRepository.save(result);
            }
        }
    }
}

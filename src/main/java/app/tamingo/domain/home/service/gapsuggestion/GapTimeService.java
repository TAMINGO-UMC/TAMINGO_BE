package app.tamingo.domain.home.service.gapsuggestion;

import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GapTimeService {

    private final GeoService geoService;

    /**
     * 일정 기반 틈새시간 목록을 추출해서 반환
     */
    protected List<GapTime> extractGapTimes(List<Schedule> schedules) {
        List<GapTime> gapTimes = new ArrayList<>();

        for (int i = 0; i < schedules.size() - 1; i++) {
            Schedule current = schedules.get(i);
            Schedule next = schedules.get(i + 1);

            LocalDateTime gapStart = current.getEndTime();
            LocalDateTime gapEnd = next.getStartTime();
            long gapMinutes = Duration.between(gapStart, gapEnd).toMinutes();

            // 15분 이상의 틈새시간만 추출
            if (gapMinutes >= 15) {
                double midLat = (current.getLatitude() + next.getLatitude()) / 2;
                double midLon = (current.getLongitude() + next.getLongitude()) / 2;
                double distance = geoService.distanceKm(
                        current.getLatitude(), current.getLongitude(),
                        next.getLatitude(), next.getLongitude());

                gapTimes.add(GapTime.builder()
                        .previousSchedule(current)
                        .nextSchedule(next)
                        .startTime(gapStart)
                        .endTime(gapEnd)
                        .availableMinutes((int) gapMinutes)
                        .midLatitude(midLat)
                        .midLongitude(midLon)
                        .distanceKm(distance)
                        .build());
            }
        }

        return gapTimes;
    }

}

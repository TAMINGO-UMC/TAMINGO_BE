package app.tamingo.domain.home.service.gapsuggestion;

import app.tamingo.domain.schedule.entity.Schedule;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GapTimeService {

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
                double distance = calculateDistance(
                        current.getLatitude(), current.getLongitude(),
                        next.getLatitude(), next.getLongitude()
                );

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


    /**
     * 두 좌표 간 거리 계산
     * @return 거리
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 지구 반경
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

}

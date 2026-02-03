package app.tamingo.domain.home.service.gapsuggestion;

import app.tamingo.domain.schedule.entity.Schedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * GapTime 클래스
 */
@Getter
@Builder
public class GapTime {
    private Schedule previousSchedule;
    private Schedule nextSchedule;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int availableMinutes;
    private double midLatitude;
    private double midLongitude;
    private double distanceKm;
}
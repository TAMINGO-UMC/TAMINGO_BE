package app.tamingo.domain.weeklyreport.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class WeeklyReportBatchScheduler {

    private final WeeklyReportBatchService weeklyReportBatchService;

    /**
     * 매주 월요일 00:05(KST) 지난주(월~일) 리포트 생성/갱신
     */
    @Scheduled(cron = "0 5 0 * * MON", zone = "Asia/Seoul")
    public void generateLastWeekReports() {
        LocalDate thisWeekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        weeklyReportBatchService.generateWeeklyReports(lastWeekStart);
    }
}

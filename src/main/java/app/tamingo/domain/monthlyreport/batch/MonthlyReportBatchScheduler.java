package app.tamingo.domain.monthlyreport.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class MonthlyReportBatchScheduler {

    private final MonthlyReportBatchService monthlyReportBatchService;

    /**
     * 매달 1일 00:10(KST) 지난달 월간 리포트 생성/갱신
     */
    @Scheduled(cron = "0 10 0 1 * *", zone = "Asia/Seoul")
    public void generateLastMonthReports() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        monthlyReportBatchService.generateMonthlyReports(lastMonth);
    }
}

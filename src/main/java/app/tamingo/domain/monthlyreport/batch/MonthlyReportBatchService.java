package app.tamingo.domain.monthlyreport.batch;

import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyReportBatchService {

    private final UserRepository userRepository;
    private final MonthlyReportBatchWorker monthlyReportBatchWorker;

    // 한명씩 돌림
    public void generateMonthlyReports(YearMonth ym) {
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();

        List<Long> userIds = userRepository.findAllUserIds();
        for (Long userId : userIds) {
            try {
                monthlyReportBatchWorker.generateOneUser(userId, ym, monthStart, monthEnd);
            } catch (Exception e) { // 유저가 실패한 경우 계속 배치를 돔
                log.warn("monthly report batch failed. userId={}, ym={}", userId, ym, e);
            }
        }
    }
}

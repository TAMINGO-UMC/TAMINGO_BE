package app.tamingo.domain.weeklyreport.batch;

import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportBatchService {

    private final UserRepository userRepository;
    private final WeeklyReportBatchWorker weeklyReportBatchWorker;

    //한명씩 돌림
    public void generateWeeklyReports(LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        List<Long> userIds = userRepository.findAllUserIds();
        for (Long userId : userIds) {
            try {
                weeklyReportBatchWorker.generateOneUser(userId, weekStartDate, weekEndDate);
            } catch (Exception e) { //유저가 실패한 경우 계속 배치를 돔
                log.warn("weekly report batch failed. userId={}, weekStart={}", userId, weekStartDate, e);
            }
        }
    }

}

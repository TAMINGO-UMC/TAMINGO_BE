package app.tamingo.domain.home.service.gapsuggestion;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.home.dto.GapSuggestionRunResponse;
import app.tamingo.domain.notification.scheduler.NotificationReservationScheduler;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GapSuggestionBatchService {

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final GapSuggestionService gapSuggestionService;
    private final NotificationReservationScheduler notificationScheduler;

    public GapSuggestionRunResponse run(LocalDate targetDate, User user) {
        LocalDate date = targetDate != null ? targetDate : LocalDate.now(TARGET_ZONE);
        return runForAllUsers(date);
    }

    public GapSuggestionRunResponse runForAllUsers() {
        return runForAllUsers(LocalDate.now(TARGET_ZONE));
    }

    // 모든 사용자들에 대해 실행
    public GapSuggestionRunResponse runForAllUsers(LocalDate targetDate) {
        // TODO : 활성 사용자만 조회하도록 변경 필요
        List<User> users = userRepository.findAll();
        List<Long> failedUserIds = new ArrayList<>();

        for (User user : users) {
            try {
                gapSuggestionService.generateGapTimeSuggestions(user, targetDate);
                notificationScheduler.reserveGapNotification(user, targetDate);
            } catch (Exception ex) {
                failedUserIds.add(user.getId());
                log.warn("[HOME] 틈새 일정 생성 실패 userId={}, date={}",
                        user.getId(), targetDate, ex);
            }
        }

        int totalUsers = users.size();
        int successUsers = totalUsers - failedUserIds.size();

        return new GapSuggestionRunResponse(
                targetDate,
                totalUsers,
                successUsers,
                failedUserIds,
                null
        );
    }

    // 특정 유저용, 테스트용
    public GapSuggestionRunResponse runForUser(User user, LocalDate targetDate) {

        try {
            gapSuggestionService.generateGapTimeSuggestions(user, targetDate);
            notificationScheduler.reserveGapNotification(user, targetDate);
            log.info("[HOME][GAP] 틈새시간 추천 성공 userId={}, date={}",
                    user.getId(), targetDate);
            return new GapSuggestionRunResponse(
                    targetDate,
                    1,
                    1,
                    List.of(user.getId()),
                    user.getId()
            );
        } catch (Exception ex) {
            log.warn("[HOME][GAP] 틈새시간 추천 실패 userId={}, date={}",
                    user.getId(), targetDate, ex);
            return new GapSuggestionRunResponse(
                    targetDate,
                    1,
                    0,
                    List.of(user.getId()),
                    user.getId()
            );
        }
    }
}

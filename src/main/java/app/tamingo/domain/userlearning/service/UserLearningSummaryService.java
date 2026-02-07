package app.tamingo.domain.userlearning.service;


import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.schedule.repository.ScheduleAiLogRepository;
import app.tamingo.domain.todo.repository.TodoAiLogRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.dto.UserSummaryResponse;
import app.tamingo.domain.userlearning.entity.DepartureAlarm;
import app.tamingo.domain.userlearning.entity.UserLearningSummary;
import app.tamingo.domain.userlearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserLearningSummaryService {

    private final UserRepository userRepository;
    private final UserLearningSummaryRepository userLearningSummaryRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final DepartureAlarmRepository departureAlarmRepository;
    private final UserLearningPatternRepository userLearningPatternRepository;
    private final ErrorLogRepository errorLogRepository;
    private final FvpHistoryRepository fvpHistoryRepository;
    private final TodoAiLogRepository todoAiLogRepository;
    private final ScheduleAiLogRepository scheduleAiLogRepository;

    // 요약 조회
    public UserSummaryResponse viewSummary(Long userId) {
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        UserLearningSummary userLearningSummary =
                userLearningSummaryRepository.findByUser(user)
                        .orElse(null);
        Long patternCount = userLearningSummary != null ? userLearningSummary.getSampleCount() : 0;
        double avgAccuracy = userLearningSummary != null ? userLearningSummary.getAvgAccuracyRate() : 0.0;
        // ai 추론 장소 개수 계산
        int fvpCount = Math.toIntExact(favoritePlaceRepository.countAiFvpByUser(user));
        return new UserSummaryResponse(patternCount, avgAccuracy, fvpCount);
    }

    // 개인화 데이터 리셋
    // 요약, 패턴, 할일/일정 로그, 출발 알림, 에러 로그, 자주가는 장소 기록 삭제
    @Transactional
    public void resetUserData(Long userId){
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 아예 제거하지 않고 0으로 리셋 - 요약/출발 알림
        UserLearningSummary userLearningSummary
                = userLearningSummaryRepository.findByUser(user)
                .orElse(null);
        DepartureAlarm departureAlarm = departureAlarmRepository.findByUser(user)
                        .orElse(null);
        if (userLearningSummary == null) {
            userLearningSummary = UserLearningSummary.of(user, 0, 0.0, 0);
        } else {
            userLearningSummary.update(0, 0.0, 0);
        }
        userLearningSummaryRepository.save(userLearningSummary);

        if (departureAlarm == null) {
            departureAlarm = DepartureAlarm.of(user, 0.0, 0);
        } else {
            departureAlarm.updateUsfAndNotifyAtMinutes(0.0, 0);
        }
        departureAlarmRepository.save(departureAlarm);

        // 아예 삭제 - 패턴/할일,일정 로그/자주가는 장소 기록/에러 로그
        userLearningPatternRepository.deleteByUser(user);
        todoAiLogRepository.deleteByUser(user);
        scheduleAiLogRepository.deleteByUser(user);
        fvpHistoryRepository.deleteByUser(user);
        errorLogRepository.deleteByUser(user);
    }

    // avgAccuracyRate 갱신
    @Transactional
    public void updateAiStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 기존 Summary 조회
        UserLearningSummary summary = userLearningSummaryRepository.findByUser(user)
                .orElseGet(() -> {
                    UserLearningSummary newSummary = UserLearningSummary.of(user, 0, 0.0, 0);
                    return userLearningSummaryRepository.save(newSummary);
                });

        // 데이터 집계
        long scheduleCount = scheduleAiLogRepository.countByUser(user);
        int scheduleSum = scheduleAiLogRepository.sumScoreByUser(user);

        long todoCount = todoAiLogRepository.countByUser(user);
        int todoSum = todoAiLogRepository.sumScoreByUser(user);

        long totalCount = scheduleCount + todoCount;
        double totalSum = (double) (scheduleSum + todoSum);

        // 평균 정확도 계산
        double newAvgAccuracy = (totalCount == 0) ? 0.0 : (totalSum / totalCount);
        // 소수점 1자리 반올림
        newAvgAccuracy = Math.round(newAvgAccuracy * 10.0) / 10.0;

        summary.update(
                summary.getSampleCount(),
                newAvgAccuracy,
                summary.getFvpCount()
        );
    }

}

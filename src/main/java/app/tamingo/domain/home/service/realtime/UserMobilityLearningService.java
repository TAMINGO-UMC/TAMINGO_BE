package app.tamingo.domain.home.service.realtime;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.home.entity.enums.ArrivedStatus;
import app.tamingo.domain.home.entity.enums.TimeSlot;
import app.tamingo.domain.home.exception.HomeErrorCode;
import app.tamingo.domain.home.redis.RealtimeSchedule;
import app.tamingo.domain.home.redis.RealtimeScheduleRepository;
import app.tamingo.domain.schedule.entity.ScheduleResult;
import app.tamingo.domain.schedule.repository.ScheduleResultRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.ErrorLog;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import app.tamingo.domain.userlearning.entity.UserLearningPattern;
import app.tamingo.domain.userlearning.entity.UserLearningSummary;
import app.tamingo.domain.userlearning.entity.enums.RouteType;
import app.tamingo.domain.userlearning.repository.ErrorLogRepository;
import app.tamingo.domain.userlearning.repository.PersonalSettingRepository;
import app.tamingo.domain.userlearning.repository.UserLearningPatternRepository;
import app.tamingo.domain.userlearning.repository.UserLearningSummaryRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMobilityLearningService {

    private static final RouteType DEFAULT_ROUTE_TYPE = RouteType.TRANSIT;

    private final UserLearningPatternRepository userLearningPatternRepository;
    private final ErrorLogRepository errorLogRepository;
    private final PersonalSettingRepository personalSettingRepository;
    private final RealtimeScheduleRepository realtimeScheduleRepository;
    private final ScheduleResultRepository scheduleResultRepository;

    // 도착 학습 정보 저장 및 요약/패턴 업데이트
    @Transactional
    public void recordArrivalLearning(
            User user,
            LocalDateTime scheduleStartTime,
            int predictedMinutes,
            int actualMinutes,
            ArrivedStatus arrivedStatus,
            double usfBefore,
            double usfAfter
    ) {
        if (user == null || scheduleStartTime == null) {
            return;
        }
        TimeSlot timeSlot = resolveTimeSlot(scheduleStartTime);

        updatePatternFromErrorLogs(user, timeSlot, DEFAULT_ROUTE_TYPE, predictedMinutes, actualMinutes);
    }

    // 오차 로그 초기 저장 : 출발지, 도착지, 지도 ETA 포함
    @Transactional
    public void saveInitialErrorLogIfFirst(Schedule schedule, String startPlaceName, int expectedDuration) {
        User user = schedule.getUser();
        String key = RealtimeSchedule.key(schedule.getId());
        RealtimeSchedule realtime = realtimeScheduleRepository.findById(key).orElse(null);
        if (realtime != null && realtime.getEtaMinutes() != null) {
            return;
        }
        if (!isErrorLogEnabled(user)) {
            return;
        }
        String arrivalPlace = schedule.getPlaceName();
        ErrorLog log = ErrorLog.of(
                startPlaceName,
                arrivalPlace,
                expectedDuration,
                0,
                0,
                ArrivedStatus.NO_SHOW,
                schedule.getUser()
        );
        errorLogRepository.save(log);
    }

    // 일정 종료 후 오차 로그 저장 (오차로그 on 일 경우)
    @Transactional
    public void saveFinalErrorLog(
            String startPlace,
            Schedule schedule,
            int expectedDuration,
            int realDuration,
            ArrivedStatus arrivedStatus
    ) {
        if (!isErrorLogEnabled(schedule.getUser())) {
            return;
        }

        String arrivalPlace = schedule.getPlaceName() != null ? schedule.getPlaceName() : "DESTINATION";
        int errorMinutes = realDuration - expectedDuration;

        ErrorLog log = ErrorLog.of(
                startPlace,
                arrivalPlace,
                expectedDuration,
                realDuration,
                errorMinutes,
                arrivedStatus,
                schedule.getUser()
        );
        errorLogRepository.save(log);
    }

    @Transactional
    public void saveScheduleResultIfFirst(
            Schedule schedule,
            int punctualityScore,
            ArrivedStatus arrivedStatus,
            LocalDateTime evaluatedAt,
            boolean navigationUsed
    ) {
        // TODO : 주간리포트 적용 후 구현
//        // 결과 중복 저장 방지
//        if(scheduleResultRepository.existsByScheduleId(schedule.getId()))
//            throw new CustomException(HomeErrorCode.SCHEDULE_RESULT_EXISTS);
//        ScheduleResult scheduleResult = ScheduleResult.of(
//                schedule,
//                punctualityScore
//        )
    }

    private boolean isErrorLogEnabled(User user) {
        PersonalSetting setting = personalSettingRepository.findByUser(user);
        return setting != null && setting.isErrorLogEnabled();
    }

    // 최신 오차로그 10개 + 현재 샘플로 패턴 업데이트
    @Transactional
    public void updatePatternFromErrorLogs(
            User user,
            TimeSlot timeSlot,
            RouteType routeType,
            int currentExpectedMinutes,
            int currentActualMinutes
    ) {
        // 최신 10개를 조회
        List<ErrorLog> recentLogs = errorLogRepository.findLatestByUserByNum(10,user);

        int totalSamples = 0;
        long diffSum = 0;
        double accuracySum = 0.0;

        for (ErrorLog log : recentLogs) {
            int expected = log.getExpectedDuration();
            int actual = log.getTotalDuration();
            if (expected <= 0 || actual <= 0) {
                continue;
            }
            int diff = actual - expected;
            diffSum += diff;
            accuracySum += accuracyRate(expected, actual);
            totalSamples++;
        }

        if (currentExpectedMinutes > 0 && currentActualMinutes > 0) {
            int diff = currentActualMinutes - currentExpectedMinutes;
            diffSum += diff;
            accuracySum += accuracyRate(currentExpectedMinutes, currentActualMinutes);
            totalSamples++;
        }

        // 보정 진행
        int avgEtaDiff = totalSamples == 0 ? 0 : (int) Math.round((double) diffSum / totalSamples);
        double avgAccuracy = totalSamples == 0 ? 0.0 : accuracySum / totalSamples;

        // 패턴이 없으면 새로 저장
        UserLearningPattern pattern = userLearningPatternRepository
                .findByUserAndTimeSlotAndRouteType(user, timeSlot, routeType)
                .orElseGet(() -> UserLearningPattern.of(user, timeSlot, routeType, 0, 0, 0.0));
        pattern.update(avgEtaDiff, totalSamples, avgAccuracy);
        userLearningPatternRepository.save(pattern);
    }

    private double accuracyRate(int expected, int actual) {
        if (expected <= 0) {
            return 0.0;
        }
        double diff = Math.abs(actual - expected);
        double rate = 1.0 - (diff / expected);
        return Math.max(0.0, Math.min(1.0, rate));
    }

    private TimeSlot resolveTimeSlot(LocalDateTime startTime) {
        return TimeSlot.fromHour(startTime.getHour());
    }
}

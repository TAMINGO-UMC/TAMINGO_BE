package app.tamingo.domain.mypage.service;

import app.tamingo.domain.calendar.entity.CalendarIntegration;
import app.tamingo.domain.calendar.repository.CalendarIntegrationRepository;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.mypage.dto.MyPageSummaryResponse;
import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.todo.repository.TodoCategoryRepository;
import app.tamingo.domain.transportpreference.repository.TransportPreferenceRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.useractivetime.entity.UserActiveTime;
import app.tamingo.domain.useractivetime.repository.UserActiveTimeRepository;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import app.tamingo.domain.userlearning.repository.PersonalSettingRepository;
import app.tamingo.domain.weeklyreport.entity.WeeklyReport;
import app.tamingo.domain.weeklyreport.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final WeeklyReportRepository weeklyReportRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;
    private final TodoCategoryRepository todoCategoryRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final CalendarIntegrationRepository calendarIntegrationRepository;
    private final UserActiveTimeRepository userActiveTimeRepository;
    private final TransportPreferenceRepository transportPreferenceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final PersonalSettingRepository personalSettingRepository;

    @Transactional(readOnly = true)
    public MyPageSummaryResponse getSummary(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        // 1) 주간 리포트(이번주 월요일 기준). 없으면 null
        LocalDate thisWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        WeeklyReport weeklyReport = weeklyReportRepository
                .findByUserIdAndWeekStartDate(userId, thisWeekMonday)
                .orElse(null);

        MyPageSummaryResponse.WeeklyReportSummary weeklySummary = (weeklyReport == null)
                ? null
                : new MyPageSummaryResponse.WeeklyReportSummary(
                weeklyReport.getWeekStartDate(),
                weeklyReport.getWeekEndDate(),
                weeklyReport.getOnTimeRate(),
                weeklyReport.getOnTimeDiff(),
                weeklyReport.getTaskCompletionRate(),
                weeklyReport.getTaskDoneCount(),
                weeklyReport.getTaskTotalCount(),
                weeklyReport.getProductivityScore(),
                weeklyReport.getProductivityGrade()
        );

        // 2) 카운트
        long scheduleCategoryCount = scheduleCategoryRepository.countByUserId(userId);
        long todoCategoryCount = todoCategoryRepository.countByUserId(userId);
        long favoritePlaceCount = favoritePlaceRepository.countByUser(user);

        // 3) 캘린더 연동
        CalendarIntegration integration = calendarIntegrationRepository.findByUserId(userId).orElse(null);
        MyPageSummaryResponse.CalendarIntegration integrationSummary = (integration == null)
                ? new MyPageSummaryResponse.CalendarIntegration(false, false, null)
                : new MyPageSummaryResponse.CalendarIntegration(true, integration.isSyncFromApple(), integration.getStatus());

        // 4) 활동 시간(없으면 기본값)
        UserActiveTime activeTime = userActiveTimeRepository.findById(userId).orElse(null);
        LocalTime activeStart = (activeTime == null || activeTime.getStartTime() == null) ? LocalTime.of(9, 0) : activeTime.getStartTime();
        LocalTime activeEnd = (activeTime == null || activeTime.getEndTime() == null) ? LocalTime.of(22, 0) : activeTime.getEndTime();

        // 5) 이동수단 우선순위
        List<String> transportPriority = transportPreferenceRepository.findAllByUserIdOrderByRankAsc(userId)
                .stream()
                .map(tp -> tp.getTransport().name())
                .toList();

        // 6) 알림(중요 알림 on/off)
        NotificationSetting notif = notificationSettingRepository.findById(userId).orElse(null);
        boolean importantAlarmEnabled = (notif == null) || notif.isDepartureAlertEnabled();

        // 7) 개인화(오차 로그 수집) 설정
        PersonalSetting personal = null;
        try {
            personal = personalSettingRepository.findByUser(user);
        } catch (Exception ignored) {}

        boolean learningDataEnabled = (personal == null) || personal.isErrorLogEnabled();

        return new MyPageSummaryResponse(
                new MyPageSummaryResponse.Profile(user.getNickname(), user.getEmail()),
                weeklySummary,
                new MyPageSummaryResponse.Counts(scheduleCategoryCount, todoCategoryCount, favoritePlaceCount),
                integrationSummary,
                new MyPageSummaryResponse.Settings(
                        activeStart,
                        activeEnd,
                        transportPriority,
                        importantAlarmEnabled,
                        learningDataEnabled
                )
        );
    }
}

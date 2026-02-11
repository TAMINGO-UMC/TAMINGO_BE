package app.tamingo.domain.home;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.home.redis.RealtimeSchedule;
import app.tamingo.domain.home.redis.RealtimeScheduleRepository;
import app.tamingo.domain.home.scheduler.ScheduleInitEnqueueScheduler;
import app.tamingo.domain.home.scheduler.ScheduleInitScheduler;
import app.tamingo.domain.home.service.realtime.ScheduleInitQueueService;
import app.tamingo.domain.kakao.dto.KakaoAddressResponseDto;
import app.tamingo.domain.kakao.service.KakaoGeoService;
import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.odsay.service.DirectionService;
import app.tamingo.domain.schedule.dto.CreateScheduleRequest;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.schedule.service.ScheduleService;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import app.tamingo.domain.userlearning.entity.UserLearningSummary;
import app.tamingo.domain.userlearning.repository.PersonalSettingRepository;
import app.tamingo.domain.userlearning.repository.UserLearningSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HomeRealtimeSchedulerIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(HomeRealtimeSchedulerIntegrationTest.class);

    private static final double FVP_LAT = 37.5665;
    private static final double FVP_LNG = 126.9780;
    private static final double DEST_LAT = 37.5700;
    private static final double DEST_LNG = 126.9900;

    @Autowired private UserRepository userRepository;
    @Autowired private PersonalSettingRepository personalSettingRepository;
    @Autowired private UserLearningSummaryRepository userLearningSummaryRepository;
    @Autowired private FavoritePlaceRepository favoritePlaceRepository;
    @Autowired private NotificationSettingRepository notificationSettingRepository;
    @Autowired private ScheduleService scheduleService;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private RealtimeScheduleRepository realtimeScheduleRepository;
    @Autowired private ScheduleInitEnqueueScheduler scheduleInitEnqueueScheduler;
    @Autowired private ScheduleInitScheduler scheduleInitScheduler;
    @Autowired private app.tamingo.domain.home.service.realtime.RealTimeScheduleService realTimeScheduleService;
    @Autowired private ScheduleInitQueueService scheduleInitQueueService;

    @MockitoBean private DirectionService directionService;
    @MockitoBean private KakaoGeoService kakaoGeoService;

    private User user;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        favoritePlaceRepository.deleteAll();
        personalSettingRepository.deleteAll();
        userLearningSummaryRepository.deleteAll();
        notificationSettingRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.of("test-scheduler@tamingo.app", "scheduler"));
        personalSettingRepository.save(PersonalSetting.of(user, true));
        userLearningSummaryRepository.save(UserLearningSummary.of(user, 0, 0.0, 0));
        NotificationSetting setting = NotificationSetting.of(user);
        setting.update(true, 0, true, true, true);
        notificationSettingRepository.save(setting);

        favoritePlaceRepository.save(FavoritePlace.of(
                user,
                "HOME",
                "서울시 중구 세종대로",
                FVP_LAT,
                FVP_LNG,
                false
        ));

        when(kakaoGeoService.getAddress(anyDouble(), anyDouble()))
                .thenReturn(new KakaoAddressResponseDto("서울시 중구 세종대로", null, null, null));
        when(directionService.calculateRoute(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new app.tamingo.domain.home.dto.DirectionResult(30, List.of()));
    }

    @Test
    void scheduler_creates_realtime_redis_entry() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusMinutes(21); // always within [now+20, now+21)
        Schedule schedule = createScheduleAt(startTime);

        log.info("[TEST] scheduleId={} startTime={}", schedule.getId(), schedule.getStartTime());

        scheduleInitEnqueueScheduler.enqueueScheduleInit();
        // simulate scheduler due-time without waiting real time
        LocalDateTime runAt = schedule.getStartTime().minusMinutes(20);
        List<Long> dueIds = scheduleInitQueueService.fetchDue(runAt, 20);
        for (Long id : dueIds) {
            realTimeScheduleService.initializeRealtimeFromSnapshot(id, runAt);
        }

        RealtimeSchedule realtime = realtimeScheduleRepository
                .findById(RealtimeSchedule.key(schedule.getId()))
                .orElse(null);

        assertThat(realtime).isNotNull();
        assertThat(realtime.getEtaMinutes()).isNotNull();
    }

    private Schedule createScheduleAt(LocalDateTime startTime) {
        LocalDate date = startTime.toLocalDate();
        LocalTime start = startTime.toLocalTime();
        LocalTime end = startTime.plusMinutes(60).toLocalTime();
        String startStr = String.format("%02d:%02d", start.getHour(), start.getMinute());
        String endStr = String.format("%02d:%02d", end.getHour(), end.getMinute());

        CreateScheduleRequest request = new CreateScheduleRequest(
                "스케줄러 테스트 일정",
                date,
                startStr,
                endStr,
                "목적지",
                "서울시 중구",
                DEST_LAT,
                DEST_LNG,
                null,
                null,
                RepeatType.NONE,
                null,
                List.of(),
                null
        );
        Long scheduleId = scheduleService.createSchedule(user.getId(), request).scheduleId();
        return scheduleRepository.findById(scheduleId).orElseThrow();
    }
}

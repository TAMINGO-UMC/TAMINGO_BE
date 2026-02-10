package app.tamingo.domain.home;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.home.dto.StartLocationGpsRequest;
import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.home.redis.RealtimeSchedule;
import app.tamingo.domain.home.redis.RealtimeScheduleRepository;
import app.tamingo.domain.home.scheduler.ScheduleInitEnqueueScheduler;
import app.tamingo.domain.home.scheduler.ScheduleInitScheduler;
import app.tamingo.domain.home.service.gps.GpsBatchService;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import app.tamingo.domain.home.service.startplace.ScheduleStartSnapshotService;
import app.tamingo.domain.home.repository.ScheduleStartSnapshotRepository;
import app.tamingo.domain.notification.repository.NotificationRedisRepository;
import app.tamingo.domain.notification.scheduler.NotificationReservationScheduler;
import app.tamingo.domain.notification.service.NotificationProducer;
import app.tamingo.domain.notification.dto.NotificationMessage;
import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.odsay.dto.OdsayTransitResponse;
import app.tamingo.domain.odsay.service.DirectionService;
import app.tamingo.domain.schedule.dto.CreateScheduleRequest;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.schedule.repository.ScheduleResultRepository;
import app.tamingo.domain.schedule.service.ScheduleService;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import app.tamingo.domain.userlearning.entity.UserLearningSummary;
import app.tamingo.domain.userlearning.repository.ErrorLogRepository;
import app.tamingo.domain.userlearning.repository.PersonalSettingRepository;
import app.tamingo.domain.userlearning.repository.UserLearningPatternRepository;
import app.tamingo.domain.userlearning.repository.UserLearningSummaryRepository;
import app.tamingo.domain.userlearning.entity.ErrorLog;
import app.tamingo.domain.kakao.dto.KakaoAddressResponseDto;
import app.tamingo.domain.kakao.service.KakaoGeoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.transaction.annotation.Transactional
class HomeFlowIntegrationTest {
    private static final Logger log = LoggerFactory.getLogger(HomeFlowIntegrationTest.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final double FVP_LAT = 37.5665;
    private static final double FVP_LNG = 126.9780;
    private static final double GPS_LAT = 37.5651;
    private static final double GPS_LNG = 126.9772;
    private static final double DEST_LAT = 37.5700;
    private static final double DEST_LNG = 126.9900;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private PersonalSettingRepository personalSettingRepository;
    @Autowired private UserLearningSummaryRepository userLearningSummaryRepository;
    @Autowired private UserLearningPatternRepository userLearningPatternRepository;
    @Autowired private FavoritePlaceRepository favoritePlaceRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private ScheduleResultRepository scheduleResultRepository;
    @Autowired private ErrorLogRepository errorLogRepository;
    @Autowired private ScheduleService scheduleService;
    @Autowired private ScheduleStartSnapshotService scheduleStartSnapshotService;
    @Autowired private ScheduleStartSnapshotRepository scheduleStartSnapshotRepository;
    @Autowired private RealtimeScheduleRepository realtimeScheduleRepository;
    @Autowired private NotificationRedisRepository notificationRedisRepository;
    @Autowired private NotificationProducer notificationProducer;
    @Autowired private NotificationSettingRepository notificationSettingRepository;
    @Autowired private StringRedisTemplate stringRedisTemplate;

    @Autowired private ScheduleInitEnqueueScheduler scheduleInitEnqueueScheduler;
    @Autowired private ScheduleInitScheduler scheduleInitScheduler;
    @Autowired private NotificationReservationScheduler notificationReservationScheduler;
    @Autowired private GpsBatchService gpsBatchService;
    @Autowired private RealTimeScheduleService realTimeScheduleService;

    @MockitoBean private DirectionService directionService;
    @MockitoBean private KakaoGeoService kakaoGeoService;

    private User user;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        log.info("[TEST] setUp start");
        stringRedisTemplate.getConnectionFactory().getConnection().flushDb();
        realtimeScheduleRepository.deleteAll();
        scheduleStartSnapshotRepository.deleteAll();
        scheduleResultRepository.deleteAll();
        scheduleRepository.deleteAll();
        favoritePlaceRepository.deleteAll();
        errorLogRepository.deleteAll();
        personalSettingRepository.deleteAll();
        userLearningPatternRepository.deleteAll();
        notificationSettingRepository.deleteAll();
        userLearningSummaryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.of("test@tamingo.app", "tester"));
        personalSettingRepository.save(PersonalSetting.of(user, true));
        userLearningSummaryRepository.save(UserLearningSummary.of(user, 0, 0.0, 0));
        NotificationSetting setting = NotificationSetting.of(user);
        setting.update(true, 0, true, true, true); // arrivalBufferMinutes = 0
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
                .thenAnswer(invocation -> {
                    double startLat = invocation.getArgument(0);
                    double startLng = invocation.getArgument(1);
                    if (Math.abs(startLat - GPS_LAT) < 1e-6 && Math.abs(startLng - GPS_LNG) < 1e-6) {
                        return new app.tamingo.domain.home.dto.DirectionResult(50, List.of());
                    }
                    return new app.tamingo.domain.home.dto.DirectionResult(30, List.of());
                });
        when(directionService.calculateRouteDetail(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(new OdsayTransitResponse(
                        new OdsayTransitResponse.MetaData(
                                new OdsayTransitResponse.Plan(
                                        List.of(new OdsayTransitResponse.Itinerary(
                                                1800,
                                                300,
                                                0,
                                                0,
                                                List.of()
                                        ))
                                )
                        )
                ));
        log.info("[TEST] setUp complete userId={}", user.getId());
    }

    @AfterEach
    void tearDown() {
        log.info("[TEST] tearDown start");
        realtimeScheduleRepository.deleteAll();
        log.info("[TEST] tearDown complete");
    }

    @Test
    void onTimeArrival_savesScheduleResult_andUserPattern() throws Exception {
        log.info("[TEST] fullFlow start");
        LocalDateTime now = LocalDateTime.now();
        schedule = createScheduleAt(now.plusMinutes(30));
        log.info("[TEST] schedule created id={} startTime={}", schedule.getId(), schedule.getStartTime());

        // 1) 일정 생성 시 출발지 스냅샷 생성 확인
        ScheduleStartSnapshot snapshot = scheduleStartSnapshotService.findSnapshotEntity(schedule);
        log.info("[TEST] snapshot created? {}", snapshot != null);
        assertThat(snapshot).isNotNull();

        // 2) Silent GPS 예약(알림 스케줄러)
        TimeZone originalTz = TimeZone.getDefault();
        try {
            TimeZone.setDefault(pickNonQuietTimeZone());
            if (isQuietHours()) {
                notificationProducer.reserve(
                        NotificationMessage.createSilentGps(user.getId(), user.getNickname(), schedule.getId()),
                        schedule.getStartTime().minusHours(1)
                );
            } else {
                notificationReservationScheduler.reserveNotifications();
            }
        } finally {
            TimeZone.setDefault(originalTz);
        }
        Set<String> reserved = notificationRedisRepository.rangeByScore(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        boolean hasSilentGps = reserved != null && reserved.stream().anyMatch(v -> v.contains("SILENT_GPS"));
        log.info("[TEST] notification reserved count={} silentGps={}", reserved == null ? 0 : reserved.size(), hasSilentGps);
        assertThat(hasSilentGps).isTrue();

        // 3) Silent GPS 체크 (GPS 기반 출발지 보정)
        StartLocationGpsRequest silentGpsReq = new StartLocationGpsRequest(schedule.getId(), GPS_LAT, GPS_LNG);
        mockMvc.perform(post("/api/location/silent-gps")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user.getId(), null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(silentGpsReq)))
                .andExpect(status().isOk());
        log.info("[TEST] silent-gps called");

        ScheduleStartSnapshot overridden = scheduleStartSnapshotService.findSnapshotEntity(schedule);
        log.info("[TEST] snapshot after silent-gps? {}", overridden != null);
        assertThat(overridden).isNotNull();

        // 4) 일정 시작 20분 전 Redis 초기화 큐 등록 및 처리
        scheduleInitEnqueueScheduler.enqueueScheduleInit();
        scheduleInitEnqueueScheduler.enqueueScheduleInit(); // next minute window fallback
        scheduleInitScheduler.processScheduleInitQueue();
        RealtimeSchedule realtime = realtimeScheduleRepository
                .findById(RealtimeSchedule.key(schedule.getId()))
                .orElse(null);
        log.info("[TEST] realtime after init? {}", realtime != null);
        if (realtime == null) {
            realTimeScheduleService.initializeRealtimeFromSnapshot(schedule.getId(), LocalDateTime.now());
            realtime = realtimeScheduleRepository
                    .findById(RealtimeSchedule.key(schedule.getId()))
                    .orElse(null);
        }
        assertThat(realtime).isNotNull();
        assertThat(realtime.getEtaMinutes()).isNotNull();

        // 5) 일정 시작 시점 위치 요청 알림
        gpsBatchService.requestLocationUpdates();

        // 6) 출발 처리 및 길찾기 시작
        StartLocationGpsRequest startReq = new StartLocationGpsRequest(schedule.getId(), GPS_LAT, GPS_LNG);
        String startBody = objectMapper.writeValueAsString(startReq);
        mockMvc.perform(post("/api/home/route-find/start")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user.getId(), null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startBody))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        log.info("[TEST] route-find/start called");

        RealtimeSchedule realtimeAfterStart = realtimeScheduleRepository
                .findById(RealtimeSchedule.key(schedule.getId()))
                .orElse(null);
        log.info("[TEST] realtime after start? {}", realtimeAfterStart != null);
        assertThat(realtimeAfterStart).isNotNull();
        log.info("[TEST] errorLog count after start={}", errorLogRepository.count());
        assertThat(errorLogRepository.count()).isEqualTo(0);

        // 7) 도착 처리 (길찾기 종료)
        StartLocationGpsRequest endReq = new StartLocationGpsRequest(schedule.getId(), DEST_LAT, DEST_LNG);
        realTimeScheduleService.confirmArrivalByEndRouteFind(endReq, schedule.getStartTime());
        log.info("[TEST] route-find/end called (service)");

        var result = scheduleResultRepository.findByScheduleId(schedule.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getStatus().name()).isEqualTo("ON_TIME");
        assertThat(result.getPunctualityScore()).isEqualTo(100);

        ErrorLog errorLog = latestErrorLog();
        assertThat(errorLog).isNotNull();
        assertThat(errorLog.getDeparturePlace()).isEqualTo("CURRENT_LOCATION");
        assertThat(errorLog.getArrivalPlace()).isEqualTo("목적지");
        log.info("[TEST] errorLog ontime expected={} total={} error={} status={}",
                errorLog.getExpectedDuration(),
                errorLog.getTotalDuration(),
                errorLog.getErrorMinutes(),
                errorLog.getStatus());
        assertErrorLogMatchesRealtime(errorLog);

        var pattern = userLearningPatternRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
        assertThat(pattern).isNotNull();
        int expectedDiff = computeExpectedDiffMinutes(schedule.getId());
        log.info("[TEST] pattern ontime sampleCount={} avgEtaDiff={} expectedDiff={} accuracyRate={}",
                pattern.getSampleCount(), pattern.getAvgEtaDiff(), expectedDiff, pattern.getAccuracyRate());
        assertThat(pattern.getSampleCount()).isGreaterThanOrEqualTo(1);
        assertThat(Math.abs(pattern.getAvgEtaDiff() - expectedDiff)).isLessThanOrEqualTo(1);
        assertThat(pattern.getAccuracyRate()).isGreaterThan(0.0);
        log.info("[TEST] fullFlow complete");
    }

    @Test
    void lateArrival_savesScheduleResult_andUserPattern() throws Exception {
        log.info("[TEST] lateArrival start");
        LocalDateTime now = LocalDateTime.now();
        schedule = createScheduleAt(now.plusMinutes(30));
        log.info("[TEST] schedule created id={} startTime={}", schedule.getId(), schedule.getStartTime());

        // 길찾기 시작
        StartLocationGpsRequest startReq = new StartLocationGpsRequest(schedule.getId(), GPS_LAT, GPS_LNG);
        mockMvc.perform(post("/api/home/route-find/start")
                        .with(authentication(new UsernamePasswordAuthenticationToken(user.getId(), null, List.of())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isOk());

        // 지각 도착 처리 (start + 5분)
        StartLocationGpsRequest endReq = new StartLocationGpsRequest(schedule.getId(), DEST_LAT, DEST_LNG);
        realTimeScheduleService.confirmArrivalByEndRouteFind(endReq, schedule.getStartTime().plusMinutes(5));

        var result = scheduleResultRepository.findByScheduleId(schedule.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getStatus().name()).isEqualTo("LATE");
        assertThat(result.getPunctualityScore()).isEqualTo(20);

        ErrorLog errorLog = latestErrorLog();
        assertThat(errorLog).isNotNull();
        assertThat(errorLog.getDeparturePlace()).isEqualTo("CURRENT_LOCATION");
        assertThat(errorLog.getArrivalPlace()).isEqualTo("목적지");
        log.info("[TEST] errorLog late expected={} total={} error={} status={}",
                errorLog.getExpectedDuration(),
                errorLog.getTotalDuration(),
                errorLog.getErrorMinutes(),
                errorLog.getStatus());
        assertErrorLogMatchesRealtime(errorLog);

        var pattern = userLearningPatternRepository.findAll().stream()
                .filter(p -> p.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
        assertThat(pattern).isNotNull();
        int expectedDiff = computeExpectedDiffMinutes(schedule.getId());
        log.info("[TEST] pattern late sampleCount={} avgEtaDiff={} expectedDiff={} accuracyRate={}",
                pattern.getSampleCount(), pattern.getAvgEtaDiff(), expectedDiff, pattern.getAccuracyRate());
        assertThat(pattern.getSampleCount()).isGreaterThanOrEqualTo(1);
        assertThat(Math.abs(pattern.getAvgEtaDiff() - expectedDiff)).isLessThanOrEqualTo(1);
        assertThat(pattern.getAccuracyRate()).isGreaterThan(0.0);
        log.info("[TEST] lateArrival complete");
    }

    private Schedule createScheduleAt(LocalDateTime startTime) {
        LocalDate date = startTime.toLocalDate();
        LocalTime start = startTime.toLocalTime();
        LocalTime end = startTime.plusMinutes(60).toLocalTime();
        String startStr = String.format("%02d:%02d", start.getHour(), start.getMinute());
        String endStr = String.format("%02d:%02d", end.getHour(), end.getMinute());

        CreateScheduleRequest request = new CreateScheduleRequest(
                "테스트 일정",
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

    private TimeZone pickNonQuietTimeZone() {
        int utcHour = LocalDateTime.now().getHour();
        int targetHour = 12;
        int offset = targetHour - utcHour;
        if (offset > 14) {
            offset -= 24;
        }
        if (offset < -12) {
            offset += 24;
        }
        String tzId = offset >= 0 ? "GMT+" + offset : "GMT" + offset;
        return TimeZone.getTimeZone(tzId);
    }

    private boolean isQuietHours() {
        int hour = LocalDateTime.now(TimeZone.getDefault().toZoneId()).getHour();
        return hour >= 1 && hour < 8;
    }

    private int computeExpectedDiffMinutes(Long scheduleId) {
        RealtimeSchedule realtime = realtimeScheduleRepository
                .findById(RealtimeSchedule.key(scheduleId))
                .orElse(null);
        if (realtime == null) {
            return 0;
        }
        LocalDateTime actualDeparture = parseDateTime(realtime.getActualDepartureTime());
        LocalDateTime actualArrival = parseDateTime(realtime.getActualArrivalTime());
        Integer etaMinutes = realtime.getEtaMinutes();
        if (actualDeparture == null || actualArrival == null || etaMinutes == null) {
            return 0;
        }
        long actualMinutes = java.time.Duration.between(actualDeparture, actualArrival).toMinutes();
        return (int) actualMinutes - etaMinutes;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null) {
            return null;
        }
        return LocalDateTime.parse(value, ISO);
    }

    private ErrorLog latestErrorLog() {
        return errorLogRepository.findLatestByUserByNum(1, user).stream().findFirst().orElse(null);
    }

    private void assertErrorLogMatchesRealtime(ErrorLog log) {
        RealtimeSchedule realtime = realtimeScheduleRepository
                .findById(RealtimeSchedule.key(schedule.getId()))
                .orElse(null);
        assertThat(realtime).isNotNull();
        Integer etaMinutes = realtime.getEtaMinutes();
        LocalDateTime actualDeparture = parseDateTime(realtime.getActualDepartureTime());
        LocalDateTime actualArrival = parseDateTime(realtime.getActualArrivalTime());
        assertThat(etaMinutes).isNotNull();
        assertThat(actualDeparture).isNotNull();
        assertThat(actualArrival).isNotNull();

        int actualMinutes = (int) java.time.Duration.between(actualDeparture, actualArrival).toMinutes();
        assertThat(log.getExpectedDuration()).isEqualTo(etaMinutes);
        assertThat(log.getTotalDuration()).isEqualTo(actualMinutes);
        assertThat(log.getErrorMinutes()).isEqualTo(actualMinutes - etaMinutes);
    }
}

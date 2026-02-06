package app.tamingo.test.initializer;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.todo.repository.TodoCategoryRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.transportpreference.entity.TransportPreference;
import app.tamingo.domain.transportpreference.entity.TransportType;
import app.tamingo.domain.transportpreference.repository.TransportPreferenceRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.useractivetime.entity.UserActiveTime;
import app.tamingo.domain.useractivetime.repository.UserActiveTimeRepository;
import app.tamingo.domain.userlearning.entity.UserLearningPattern;
import app.tamingo.domain.userlearning.entity.UserLearningSummary;
import app.tamingo.domain.userlearning.entity.enums.RouteType;
import app.tamingo.domain.home.entity.enums.ArrivedStatus;
import app.tamingo.domain.userlearning.repository.UserLearningPatternRepository;
import app.tamingo.domain.userlearning.repository.UserLearningSummaryRepository;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import app.tamingo.domain.userlearning.repository.PersonalSettingRepository;
import app.tamingo.domain.userlearning.entity.ErrorLog;
import app.tamingo.domain.userlearning.repository.ErrorLogRepository;
import app.tamingo.domain.home.entity.enums.TimeSlot;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    public static final String TEST_EMAIL = "test@tamingo.dev";

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final UserRepository userRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;
    private final TodoCategoryRepository todoCategoryRepository;
    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserActiveTimeRepository userActiveTimeRepository;
    private final TransportPreferenceRepository transportPreferenceRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserLearningSummaryRepository userLearningSummaryRepository;
    private final UserLearningPatternRepository userLearningPatternRepository;
    private final PersonalSettingRepository personalSettingRepository;
    private final ErrorLogRepository errorLogRepository;

    @Override
    @Transactional
    public void run(String... args) {
        User user = userRepository.findByEmail(TEST_EMAIL)
                .orElseGet(() -> userRepository.save(User.of(TEST_EMAIL, "테스트 유저")));

        ScheduleCategory scheduleCategory = ensureScheduleCategory(user);
        TodoCategory todoCategory = ensureTodoCategory(user);

        ensureOnboardingEntities(user);
        ensureFavoritePlaces(user);
        ensureUserLearningData(user);

        LocalDate today = LocalDate.now(TARGET_ZONE);
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        List<Schedule> todaySchedules =
                scheduleRepository.findAllByUserAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(
                        user, dayStart, dayEnd
                );

        Schedule morning = ensureSchedule(
                todaySchedules,
                user,
                scheduleCategory,
                "TEST-아침 미팅",
                today.atTime(9, 30),
                today.atTime(10, 30),
                "테스트 카페",
                "서울특별시 중구 세종대로 110",
                37.5663,
                126.9781
        );

        Schedule lunch = ensureSchedule(
                todaySchedules,
                user,
                scheduleCategory,
                "TEST-점심 약속",
                today.atTime(12, 30),
                today.atTime(13, 30),
                "테스트 식당",
                "서울특별시 중구 세종대로 135",
                37.5672,
                126.9775
        );

        Schedule afternoon = ensureSchedule(
                todaySchedules,
                user,
                scheduleCategory,
                "TEST-오후 업무",
                today.atTime(15, 0),
                today.atTime(17, 0),
                "테스트 오피스",
                "서울특별시 중구 세종대로 99",
                37.5659,
                126.9768
        );

        ensureSchedule(
                todaySchedules,
                user,
                scheduleCategory,
                "TEST-저녁 일정",
                today.atTime(20, 0),
                today.atTime(21, 0),
                "테스트 라운지",
                "서울특별시 중구 세종대로 160",
                37.5680,
                126.9764
        );

        Todo todoWithPlaceAndDuration = ensureTodo(
                user,
                todoCategory,
                "TEST-서류 제출",
                today,
                "테스트 우체국",
                "서울특별시 중구 세종대로 120",
                37.5669,
                126.9786,
                20
        );

        ensureTodo(
                user,
                todoCategory,
                "TEST-타이어 공기압",
                today,
                null,
                null,
                null,
                null,
                15
        );

        ensureTodo(
                user,
                todoCategory,
                "TEST-책 반납",
                today,
                "테스트 도서관",
                "서울특별시 중구 세종대로 171",
                37.5682,
                126.9770,
                null
        );

        ensureTodo(
                user,
                todoCategory,
                "TEST-마트 장보기",
                today,
                "테스트 마트",
                "서울특별시 중구 세종대로 102",
                37.5655,
                126.9779,
                40
        );

        ensureTodo(
                user,
                todoCategory,
                "TEST-영수증 정리",
                today,
                null,
                null,
                null,
                null,
                30
        );

        if (todoWithPlaceAndDuration.getSchedule() == null) {
            todoWithPlaceAndDuration.connectSchedule(lunch);
            todoRepository.save(todoWithPlaceAndDuration);
        }

        System.out.println("[TEST] data ready: user=" + user.getId()
                + ", schedules=[" + morning.getId() + ", " + afternoon.getId() + "]");
    }

    private ScheduleCategory ensureScheduleCategory(User user) {
        if (scheduleCategoryRepository.existsByUserAndName(user, "TEST-일정")) {
            return scheduleCategoryRepository.findAllByUser(user).stream()
                    .filter(cat -> "TEST-일정".equals(cat.getName()))
                    .findFirst()
                    .orElseThrow();
        }
        return scheduleCategoryRepository.save(
                ScheduleCategory.of(
                        "TEST-일정",
                        "icon-briefcase",
                        "#1E40AF",
                        user
                )
        );
    }

    private TodoCategory ensureTodoCategory(User user) {
        if (todoCategoryRepository.existsByUserAndName(user, "TEST-할일")) {
            return todoCategoryRepository.findAllByUser(user).stream()
                    .filter(cat -> "TEST-할일".equals(cat.getName()))
                    .findFirst()
                    .orElseThrow();
        }
        return todoCategoryRepository.save(
                TodoCategory.of(
                        "TEST-할일",
                        "icon-check",
                        "#15803D",
                        user
                )
        );
    }

    private void ensureOnboardingEntities(User user) {
        userActiveTimeRepository.findById(user.getId()).ifPresentOrElse(
                activeTime -> activeTime.update(
                        LocalTime.of(8, 0),
                        LocalTime.of(23, 0),
                        true, true, true, true, true, true
                ),
                () -> userActiveTimeRepository.save(
                        UserActiveTime.of(
                                user,
                                LocalTime.of(8, 0),
                                LocalTime.of(23, 0),
                                true, true, true, true, true, true
                        )
                )
        );

        List<TransportPreference> prefs =
                transportPreferenceRepository.findAllByUserIdOrderByRankAsc(user.getId());
        if (prefs.size() != 3) {
            transportPreferenceRepository.deleteAll(prefs);
            transportPreferenceRepository.saveAll(List.of(
                    TransportPreference.of(user, TransportType.SUBWAY, 1),
                    TransportPreference.of(user, TransportType.BUS, 2),
                    TransportPreference.of(user, TransportType.WALK, 3)
            ));
        }

        NotificationSetting setting = notificationSettingRepository.findById(user.getId())
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.of(user)));
        setting.update(
                true,
                20,
                true,
                true,
                true,
                true
        );

        PersonalSetting personalSetting = personalSettingRepository.findByUser(user);
        if (personalSetting == null) {
            personalSettingRepository.save(PersonalSetting.of(user, true));
        }

        if (!user.isOnboardingCompleted()) {
            user.completeOnboarding();
            userRepository.save(user);
        }
    }

    private void ensureFavoritePlaces(User user) {
        List<FavoritePlace> existing = favoritePlaceRepository.findAllByUser(user);
        Set<String> names = existing.stream()
                .map(FavoritePlace::getName)
                .collect(Collectors.toSet());

        if (!names.contains("테스트 집")) {
            favoritePlaceRepository.save(FavoritePlace.of(
                    user,
                    "테스트 집",
                    "서울특별시 중구 세종대로 120",
                    37.5668,
                    126.9782,
                    false
            ));
        }

        if (!names.contains("테스트 체육관")) {
            favoritePlaceRepository.save(FavoritePlace.of(
                    user,
                    "테스트 체육관",
                    "서울특별시 중구 세종대로 150",
                    37.5676,
                    126.9771,
                    false
            ));
        }

        if (!names.contains("테스트 마트")) {
            favoritePlaceRepository.save(FavoritePlace.of(
                    user,
                    "테스트 마트",
                    "서울특별시 중구 세종대로 102",
                    37.5655,
                    126.9779,
                    false
            ));
        }
    }

    private void ensureUserLearningData(User user) {
        int fvpCount = favoritePlaceRepository.findAllByUser(user).size();

        userLearningSummaryRepository.findByUser(user).ifPresentOrElse(
                summary -> summary.update(12, 0.82, fvpCount),
                () -> userLearningSummaryRepository.save(
                        UserLearningSummary.of(user, 12, 0.82, fvpCount)
                )
        );

        userLearningPatternRepository.deleteByUser(user);
        userLearningPatternRepository.saveAll(List.of(
                UserLearningPattern.of(user, TimeSlot.MORNING, RouteType.TRANSIT, -3, 6, 0.86),
                UserLearningPattern.of(user, TimeSlot.DAY, RouteType.TRANSIT, 2, 4, 0.78),
                UserLearningPattern.of(user, TimeSlot.EVENING, RouteType.WALK_TRANSIT, 1, 5, 0.81),
                UserLearningPattern.of(user, TimeSlot.NIGHT, RouteType.WALK, -1, 3, 0.9)
        ));

        if (errorLogRepository.findLatestByUserByNum(1, user).isEmpty()) {
            errorLogRepository.saveAll(List.of(
                    ErrorLog.of("테스트 집", "테스트 오피스", 35, 38, 3, ArrivedStatus.ON_TIME, user),
                    ErrorLog.of("테스트 카페", "테스트 식당", 15, 22, 7, ArrivedStatus.LATE, user),
                    ErrorLog.of("테스트 오피스", "테스트 라운지", 25, 20, -5, ArrivedStatus.EARLY, user)
            ));
        }
    }

    private Schedule ensureSchedule(
            List<Schedule> todaySchedules,
            User user,
            ScheduleCategory category,
            String title,
            LocalDateTime start,
            LocalDateTime end,
            String placeName,
            String address,
            Double latitude,
            Double longitude
    ) {
        for (Schedule schedule : todaySchedules) {
            if (title.equals(schedule.getTitle())) {
                return schedule;
            }
        }

        Schedule schedule = Schedule.of(
                user,
                category,
                title,
                start,
                end,
                placeName,
                address,
                latitude,
                longitude,
                null,
                null,
                "TEST 데이터"
        );
        return scheduleRepository.save(schedule);
    }

    private Todo ensureTodo(
            User user,
            TodoCategory category,
            String title,
            LocalDate targetDate,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            Integer duration
    ) {
        List<Todo> recentTodos = todoRepository.findTop20ByUserOrderByIdDesc(user);
        for (Todo todo : recentTodos) {
            if (title.equals(todo.getTitle())) {
                return todo;
            }
        }

        Todo todo = Todo.of(
                user,
                category,
                title,
                targetDate,
                placeName,
                address,
                latitude,
                longitude,
                duration,
                null,
                null
        );
        return todoRepository.save(todo);
    }
}

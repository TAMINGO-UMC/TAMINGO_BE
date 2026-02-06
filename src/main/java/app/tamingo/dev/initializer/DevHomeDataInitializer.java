package app.tamingo.dev.initializer;

import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.StartSourceType;
import app.tamingo.domain.home.entity.enums.SuggestionPlanType;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.home.repository.ScheduleStartSnapshotRepository;
import app.tamingo.domain.home.repository.SuggestionLearningRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.todo.repository.TodoCategoryRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevHomeDataInitializer implements CommandLineRunner {

    private static final String DEV_EMAIL = "master@tamingo.dev";

    private final UserRepository userRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;
    private final TodoCategoryRepository todoCategoryRepository;
    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final SuggestionLearningRepository suggestionLearningRepository;
    private final ScheduleStartSnapshotRepository scheduleStartSnapshotRepository;

    @Override
    @Transactional
    public void run(String... args) {
        User user = userRepository.findByEmail(DEV_EMAIL)
                .orElseGet(() -> userRepository.save(User.of(DEV_EMAIL, "마스터")));

        ScheduleCategory scheduleCategory = ensureScheduleCategory(user);
        TodoCategory todoCategory = ensureTodoCategory(user);

        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Schedule> todaySchedules =
                scheduleRepository.findAllByUserAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(
                        user, dayStart, dayEnd
                );

        Schedule mainSchedule = findScheduleByTitle(todaySchedules, "DEV-홈 테스트 일정");
        if (mainSchedule == null) {
            LocalDateTime start = today.atTime(LocalTime.of(14, 0));
            LocalDateTime end = start.plusHours(1);
            mainSchedule = Schedule.of(
                    user,
                    scheduleCategory,
                    "DEV-홈 테스트 일정",
                    start,
                    end,
                    "DEV 오피스",
                    "서울특별시 중구 세종대로 110",
                    37.5665,
                    126.9780,
                    null,
                    null,
                    "홈 화면 테스트용 일정"
            );
            scheduleRepository.save(mainSchedule);
        }

        Schedule secondarySchedule = findScheduleByTitle(todaySchedules, "DEV-저녁 일정");
        if (secondarySchedule == null) {
            LocalDateTime start = today.atTime(LocalTime.of(18, 30));
            LocalDateTime end = start.plusMinutes(90);
            secondarySchedule = Schedule.of(
                    user,
                    scheduleCategory,
                    "DEV-저녁 일정",
                    start,
                    end,
                    "DEV 약속 장소",
                    "서울특별시 종로구 세종대로 175",
                    37.5720,
                    126.9769,
                    null,
                    null,
                    "홈 화면 목록 테스트용"
            );
            scheduleRepository.save(secondarySchedule);
        }

        Todo linkedTodo = ensureTodo(
                user,
                todoCategory,
                "DEV-연결된 할일",
                "DEV 문구점",
                null,
                null
        );
        if (linkedTodo.getSchedule() == null) {
            linkedTodo.connectSchedule(mainSchedule);
            todoRepository.save(linkedTodo);
        }

        Todo gapTodo = ensureTodo(
                user,
                todoCategory,
                "DEV-틈새 할일",
                "DEV 카페",
                37.5658,
                126.9784
        );

        Todo detourTodo = ensureTodo(
                user,
                todoCategory,
                "DEV-동선 할일",
                "DEV 서점",
                37.5700,
                126.9820
        );

        ensureGapTimeSuggestion(user, mainSchedule, gapTodo, scheduleCategory);
        ensureRouteDetourSuggestion(user, mainSchedule, detourTodo);

        ensureSnapshot(mainSchedule);

        System.out.println("[DEV] home dummy data ready: user=" + user.getId()
                + ", schedule=" + mainSchedule.getId());
    }

    private ScheduleCategory ensureScheduleCategory(User user) {
        if (scheduleCategoryRepository.existsByUserAndName(user, "DEV-일정")) {
            return scheduleCategoryRepository.findAllByUser(user).stream()
                    .filter(cat -> "DEV-일정".equals(cat.getName()))
                    .findFirst()
                    .orElseThrow();
        }
        ScheduleCategory category = ScheduleCategory.of(
                "DEV-일정",
                "icon-briefcase",
                "#2B6CB0",
                user
        );
        return scheduleCategoryRepository.save(category);
    }

    private TodoCategory ensureTodoCategory(User user) {
        if (todoCategoryRepository.existsByUserAndName(user, "DEV-할일")) {
            return todoCategoryRepository.findAllByUser(user).stream()
                    .filter(cat -> "DEV-할일".equals(cat.getName()))
                    .findFirst()
                    .orElseThrow();
        }
        TodoCategory category = TodoCategory.of(
                "DEV-할일",
                "icon-check",
                "#2F855A",
                user
        );
        return todoCategoryRepository.save(category);
    }

    private Schedule findScheduleByTitle(List<Schedule> schedules, String title) {
        return schedules.stream()
                .filter(schedule -> title.equals(schedule.getTitle()))
                .findFirst()
                .orElse(null);
    }

    private Todo ensureTodo(
            User user,
            TodoCategory todoCategory,
            String title,
            String placeName,
            Double latitude,
            Double longitude
    ) {
        List<Todo> recentTodos = todoRepository.findTop20ByUserOrderByIdDesc(user);
        for (Todo todo : recentTodos) {
            if (title.equals(todo.getTitle())) {
                return todo;
            }
        }
        Todo todo = Todo.of(
                user,
                todoCategory,
                title,
                LocalDate.now(),
                placeName,
                null,
                latitude,
                longitude,
                30,
                null,
                null
        );
        return todoRepository.save(todo);
    }

    private void ensureGapTimeSuggestion(
            User user,
            Schedule schedule,
            Todo linkedTodo,
            ScheduleCategory suggestedCategory
    ) {
        if (suggestionLearningRepository.existsLinkedSuggestion(
                user,
                SuggestionType.GAP_TIME,
                linkedTodo.getId(),
                schedule
        )) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.plusMinutes(45);
        if (!startTime.toLocalDate().equals(LocalDate.now())) {
            startTime = LocalDate.now().atTime(13, 0);
        }
        LocalDateTime endTime = startTime.plusMinutes(30);

        SuggestionLearning suggestion = SuggestionLearning.of(
                user,
                "DEV-틈새 추천",
                schedule,
                SuggestionType.GAP_TIME,
                SuggestionPlanType.TODO_BASED_SCHEDULE,
                "DEV 카페",
                37.5658,
                126.9784,
                "틈새시간에 들를 수 있는 추천입니다.",
                startTime,
                endTime,
                30,
                null,
                linkedTodo.getId(),
                suggestedCategory.getId()
        );
        suggestionLearningRepository.save(suggestion);
    }

    private void ensureRouteDetourSuggestion(
            User user,
            Schedule schedule,
            Todo linkedTodo
    ) {
        if (suggestionLearningRepository.existsLinkedSuggestion(
                user,
                SuggestionType.ROUTE_DETOUR,
                linkedTodo.getId(),
                schedule
        )) {
            return;
        }

        SuggestionLearning suggestion = SuggestionLearning.of(
                user,
                "DEV-동선 추천",
                schedule,
                SuggestionType.ROUTE_DETOUR,
                SuggestionPlanType.TODO_BASED_TODO,
                "DEV 서점",
                37.5700,
                126.9820,
                "이동 경로에 맞는 추천입니다.",
                schedule.getStartTime(),
                schedule.getEndTime(),
                null,
                8,
                linkedTodo.getId(),
                null
        );
        suggestionLearningRepository.save(suggestion);
    }

    private void ensureSnapshot(Schedule schedule) {
        if (scheduleStartSnapshotRepository.existsBySchedule(schedule)) {
            return;
        }
        ScheduleStartSnapshot snapshot = ScheduleStartSnapshot.of(
                schedule,
                StartSourceType.GPS,
                0L,
                37.5651,
                126.9895,
                "DEV 출발지",
                LocalDateTime.now().minusMinutes(30),
                false
        );
        snapshot.updateMapEtaMinutes(25);
        scheduleStartSnapshotRepository.save(snapshot);
    }
}

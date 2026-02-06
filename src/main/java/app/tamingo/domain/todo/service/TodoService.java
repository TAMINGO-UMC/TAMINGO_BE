package app.tamingo.domain.todo.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.schedule.dto.ScheduleSummaryResponse;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.dto.*;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoAiLog;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.todo.exception.TodoErrorCode;
import app.tamingo.domain.todo.repository.TodoAiLogRepository;
import app.tamingo.domain.todo.repository.TodoCategoryRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.service.UserLearningSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoCategoryRepository todoCategoryRepository;
    private final TodoAiLogRepository todoAiLogRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserLearningSummaryService userLearningSummaryService;

    /**
     * 할일 생성(AiLog 100점 부여)
     */
    @Transactional
    public CreateTodoResponse create(Long userId, CreateTodoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 카테고리 조회
        TodoCategory category = null;
        if (request.todoCategoryId() != null) {
            category = todoCategoryRepository.findByIdAndUser(request.todoCategoryId(), user)
                    .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_CATEGORY_NOT_FOUND));
        }

        // 할 일 저장
        Todo todo = todoRepository.save(request.toEntity(user, category));

        // AI 로그 저장 (최초 생성 시 점수 100점)
        if (request.aiSource() != null) {
            saveInitialAiLog(user, todo, category, request.aiSource());

            userLearningSummaryService.updateAiStats(userId);
        }

        return new CreateTodoResponse(todo.getId());
    }

    private void saveInitialAiLog(User user, Todo todo, TodoCategory category, CreateTodoRequest.AiInferenceContent aiSource) {

        // 사용자 지정 값 추출
        String userCategoryName = (category != null) ? category.getName() : null;
        String userPlaceName = todo.getPlaceName();
        Integer userDuration = todo.getDuration();

        TodoAiLog log = TodoAiLog.of(
                user,
                todo,
                aiSource.aiSuggestedCategoryName(),
                aiSource.aiSuggestedPlaceName(),
                aiSource.aiSuggestedDuration(),
                userCategoryName,
                userPlaceName,
                userDuration,
                100
        );

        todoAiLogRepository.save(log);
    }

    /**
     * 할 일 상세 조회
     */
    public TodoDetailResponse getTodoDetail(Long userId, Long todoId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

        // 본인 확인
        if (!todo.getUser().getId().equals(userId)) {
            throw new CustomException(TodoErrorCode.TODO_NOT_OWNER);
        }

        // Nearby Schedules 조회 (위치 정보가 있을 때만 실행)
        List<Schedule> nearbySchedules = new ArrayList<>();
        if (todo.getLatitude() != null && todo.getLongitude() != null) {
            nearbySchedules = getNearbySchedules(userId, todo.getLatitude(), todo.getLongitude());
        }

        // 현재일부터 +7일 동안의 일정 조회
        List<Schedule> weeklySchedules = getWeeklySchedules(userId);

        // 중복 제거
        if (!nearbySchedules.isEmpty()) {
            Set<Long> nearbyIds = nearbySchedules.stream()
                    .map(Schedule::getId)
                    .collect(Collectors.toSet());

            weeklySchedules = weeklySchedules.stream()
                    .filter(s -> !nearbyIds.contains(s.getId()))
                    .toList();
        }

        // 자주 가는 장소 추천 여부
        boolean isFavoriteRecommendation = checkFavoriteRecommendation(user, todo.getPlaceName());

        // 응답 생성 (Nearby + Weekly)
        return TodoDetailResponse.of(todo, nearbySchedules, weeklySchedules, isFavoriteRecommendation);
    }

    private boolean checkFavoriteRecommendation(User user, String placeName) {
        if (placeName == null || placeName.isBlank()) {
            return false;
        }

        boolean alreadyExists = favoritePlaceRepository.existsByUserAndName(user, placeName);
        if (alreadyExists) {
            return false;
        }

        int scheduleCount = scheduleRepository.countByUserAndPlaceName(user, placeName);
        int todoCount = todoRepository.countByUserAndPlaceNameAndIsLocationConfirmedTrue(user, placeName);

        return (scheduleCount + todoCount) >= 1;
    }

    /**
     * 할 일 수정
     */
    @Transactional
    public void update(Long userId, Long todoId, UpdateTodoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

        if (!todo.getUser().getId().equals(userId)) {
            throw new CustomException(TodoErrorCode.TODO_NOT_OWNER);
        }

        RepeatType oldRepeatType = todo.getRepeatType();

        // 카테고리 조회
        TodoCategory category = null;
        if (request.todoCategoryId() != null) {
            category = todoCategoryRepository.findByIdAndUser(request.todoCategoryId(), user)
                    .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_CATEGORY_NOT_FOUND));
        }

        todo.update(
                request.title(),
                request.targetDate(),
                category,
                request.placeName(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.duration(),
                request.repeatType(),
                request.repeatEndDate()
        );

        // 일정 연결 및 해제
        if (request.linkedScheduleId() != null) {
            // 연결 요청이 있는 경우
            Schedule schedule = scheduleRepository.findById(request.linkedScheduleId())
                    .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

            if (!schedule.getUser().getId().equals(userId)) {
                throw new CustomException(ScheduleErrorCode.SCHEDULE_NOT_OWNER);
            }
            // 연결 + 날짜 동기화
            todo.connectSchedule(schedule);

        } else {
            // 연결 해제 요청 (null)
            todo.disconnectSchedule();
        }

        // AI 로그 업데이트
        updateAiLogScore(todo, category, request);

        userLearningSummaryService.updateAiStats(userId);

        // 반복 할 일 생성 로직 (NONE -> REPEAT 변경 시에만 작동)
        if (oldRepeatType == RepeatType.NONE
                && request.repeatType() != null
                && request.repeatType() != RepeatType.NONE
                && request.repeatEndDate() != null
                && todo.getTargetDate() != null) {

            createRecurringTodos(user, todo, request.repeatType(), request.repeatEndDate());
        }
    }

    private void createRecurringTodos(User user, Todo original, RepeatType repeatType, LocalDate repeatEndDate) {
        List<Todo> newTodos = new ArrayList<>();
        LocalDate currentDate = original.getTargetDate();

        // 최대 1년까지만 생성 (무한 루프 방지)
        LocalDate limitDate = original.getTargetDate().plusYears(1);
        LocalDate effectiveEndDate = repeatEndDate.isAfter(limitDate) ? limitDate : repeatEndDate;

        // 첫 번째 반복 날짜 계산 (수정된 원본의 다음 주기부터 생성)
        currentDate = getNextDate(currentDate, repeatType);

        while (!currentDate.isAfter(effectiveEndDate)) {
            Todo newTodo = Todo.createRecurring(
                    user,
                    original.getTodoCategory(),
                    original.getTitle(),
                    currentDate,
                    original.getPlaceName(),
                    original.getAddress(),
                    original.getLatitude(),
                    original.getLongitude(),
                    original.getDuration(),
                    repeatType,
                    repeatEndDate,
                    true
            );

            newTodos.add(newTodo);

            currentDate = getNextDate(currentDate, repeatType);
        }

        if (!newTodos.isEmpty()) {
            todoRepository.saveAll(newTodos);
        }
    }

    private LocalDate getNextDate(LocalDate current, RepeatType type) {
        if (type == RepeatType.DAILY) return current.plusDays(1);
        if (type == RepeatType.WEEKLY) return current.plusWeeks(1);
        if (type == RepeatType.MONTHLY) return current.plusMonths(1);
        return current.plusDays(1); // 기본값 (도달하지 않음)
    }

    private void updateAiLogScore(Todo todo, TodoCategory category, UpdateTodoRequest request) {
        todoAiLogRepository.findByTodo(todo).ifPresent(log -> {
            String categoryName = (category != null) ? category.getName() : null;

            // 사용자 선택값 업데이트 및 점수 계산 (Entity 내부에서 1회 제한 처리됨)
            log.updateUserSelection(
                    categoryName,
                    request.placeName(),
                    request.duration()
            );
        });
    }

    @Transactional
    public void updateCheckStatus(Long userId, Long todoId, boolean isChecked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(TodoErrorCode.TODO_NOT_FOUND));

        if (!todo.getUser().getId().equals(userId)) {
            throw new CustomException(TodoErrorCode.TODO_NOT_OWNER);
        }

        if (isChecked && todo.getTargetDate() == null) {
            todo.updateTargetDate(LocalDate.now());
        }

        // 상태 변경
        todo.updateCheckStatus(isChecked);
    }

    /**
     * 할 일 장소 수정 시 일정 추천
     */
    public RecommendScheduleResponse recommendSchedules(Long userId, RecommendScheduleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 주변 일정 조회
        List<Schedule> nearbySchedules = getNearbySchedules(userId, request.latitude(), request.longitude());

        // +7일 일정 조회
        List<Schedule> candidateSchedules = getWeeklySchedules(userId);

        // 중복 제거
        if (!nearbySchedules.isEmpty()) {
            Set<Long> nearbyIds = nearbySchedules.stream()
                    .map(Schedule::getId)
                    .collect(Collectors.toSet());

            candidateSchedules = candidateSchedules.stream()
                    .filter(s -> !nearbyIds.contains(s.getId()))
                    .toList();
        }

        // 자주 가는 장소 추천 여부
        boolean isFavorite = checkFavoriteRecommendation(user, request.placeName());

        // DTO 변환
        List<ScheduleSummaryResponse> nearbyDtos = nearbySchedules.stream()
                .map(ScheduleSummaryResponse::from)
                .toList();

        List<ScheduleSummaryResponse> candidateDtos = candidateSchedules.stream()
                .map(ScheduleSummaryResponse::from)
                .toList();

        return new RecommendScheduleResponse(nearbyDtos, candidateDtos, isFavorite);
    }

    /**
     * 반경 2km 이내의 미래 일정 조회
     */
    private List<Schedule> getNearbySchedules(Long userId, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return Collections.emptyList();
        }
        double radius = 0.02;
        return scheduleRepository.findNearbySchedules(
                userId,
                latitude,
                longitude,
                latitude - radius,
                latitude + radius,
                longitude - radius,
                longitude + radius,
                LocalDateTime.now()
        );
    }

    /**
     * 오늘 ~ 7일 후까지의 일정 조회
     */
    private List<Schedule> getWeeklySchedules(Long userId) {
        LocalDate today = LocalDate.now();
        return scheduleRepository.findSchedulesInPeriod(
                userId,
                today.atStartOfDay(),
                today.plusDays(7).atTime(23, 59, 59)
        );
    }

    /**
     * 날짜 지정 + 미지정 할 일 조회
     */
    public DailyTodoListResponse getDailyTodos(Long userId, LocalDate date) {

        if (!userRepository.existsById(userId)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND);
        }

        // 해당 날짜 할 일 조회 (카테고리 정렬)
        List<TodoListResponse> dailyTodos = todoRepository.findDailyTodos(userId, date)
                .stream()
                .map(TodoListResponse::from)
                .toList();

        // 날짜 미지정 할 일 조회 (미완료만)
        List<TodoListResponse> backlogTodos = todoRepository.findBacklogTodos(userId)
                .stream()
                .map(TodoListResponse::from)
                .toList();

        return DailyTodoListResponse.of(dailyTodos, backlogTodos);
    }

}

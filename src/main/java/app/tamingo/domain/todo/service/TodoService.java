package app.tamingo.domain.todo.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.dto.CreateTodoRequest;
import app.tamingo.domain.todo.dto.CreateTodoResponse;
import app.tamingo.domain.todo.dto.TodoDetailResponse;
import app.tamingo.domain.todo.dto.UpdateTodoRequest;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoAiLog;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.todo.exception.TodoErrorCode;
import app.tamingo.domain.todo.repository.TodoAiLogRepository;
import app.tamingo.domain.todo.repository.TodoCategoryRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            double radius = 0.02; // 약 2km 반경

            nearbySchedules = scheduleRepository.findNearbySchedules(
                    userId,
                    todo.getLatitude(),
                    todo.getLongitude(),
                    todo.getLatitude() - radius,
                    todo.getLatitude() + radius,
                    todo.getLongitude() - radius,
                    todo.getLongitude() + radius,
                    LocalDateTime.now() // 현재 시간 이후의 일정만
            );
        }

        // Weekly Schedules 조회 (오늘 ~ 7일 후)
        LocalDate today = LocalDate.now();
        List<Schedule> weeklySchedules = scheduleRepository.findSchedulesInPeriod(
                userId,
                today.atStartOfDay(),
                today.plusDays(7).atTime(23, 59, 59)
        );

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

        return (scheduleCount + todoCount) >= 3;
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

        // 상태 변경
        todo.updateCheckStatus(isChecked);
    }

}

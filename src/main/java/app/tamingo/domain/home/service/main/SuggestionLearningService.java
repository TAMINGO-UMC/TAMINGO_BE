package app.tamingo.domain.home.service.main;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionPlanType;
import app.tamingo.domain.home.exception.HomeErrorCode;
import app.tamingo.domain.home.repository.SuggestionLearningRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuggestionLearningService {

    private final SuggestionLearningRepository suggestionLearningRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;

    // 틈새시간 일정 학습 수락 처리
    @Transactional
    public void acceptSuggestion(Long userId, Long suggestionLearningId) {
        User user = userRepository.getReferenceById(userId);
        SuggestionLearning suggestionLearning = loadOwnedSuggestion(user, suggestionLearningId);

        SuggestionPlanType planType = suggestionLearning.getPlanType();
        if (planType == null) {
            throw new CustomException(HomeErrorCode.SUGGESTION_LEARNING_NOT_FOUND);
        }

        switch (planType) {
            case SCHEDULE_BASED_SCHEDULE -> acceptScheduleBasedSchedule(suggestionLearning);
            case TODO_BASED_SCHEDULE -> acceptTodoBasedSchedule(suggestionLearning);
            case TODO_BASED_TODO -> acceptTodoBasedTodo(suggestionLearning);
        }

        // 수락할 경우, 바로 삭제처리
        suggestionLearningRepository.delete(suggestionLearning);

    }

    // 틈새시간 일정 학습 거절 처리
    @Transactional
    public void rejectSuggestion(Long userId, Long suggestionLearningId) {
        User user = userRepository.getReferenceById(userId);
        SuggestionLearning suggestionLearning = loadOwnedSuggestion(user, suggestionLearningId);
        suggestionLearningRepository.delete(suggestionLearning);
    }

    // 사용자가 소유한 틈새시간 일정 학습 조회
    private SuggestionLearning loadOwnedSuggestion(User user, Long suggestionLearningId) {
        SuggestionLearning suggestionLearning = suggestionLearningRepository.findById(suggestionLearningId)
                .orElseThrow(() -> new CustomException(HomeErrorCode.SUGGESTION_LEARNING_NOT_FOUND));
        if (!suggestionLearning.getUser().getId().equals(user.getId())) {
            throw new CustomException(HomeErrorCode.SUGGESTION_UNAUTHORIZED_ACCESS);
        }
        return suggestionLearning;
    }

    private void acceptScheduleBasedSchedule(SuggestionLearning suggestionLearning) {
        Schedule baseSchedule = suggestionLearning.getSchedule();
        ScheduleCategory category = resolveCategory(suggestionLearning, baseSchedule);

        Schedule schedule = Schedule.of(
                suggestionLearning.getUser(),
                category,


                baseSchedule != null ? baseSchedule.getTitle() : "추천 일정",
                suggestionLearning.getStartTime(),
                suggestionLearning.getEndTime(),
                suggestionLearning.getPlaceName(),
                null,
                suggestionLearning.getLatitude(),
                suggestionLearning.getLongitude(),
                null,
                null,
                suggestionLearning.getAiComment()
        );
        schedule.disableNavigation();
        scheduleRepository.save(schedule);
    }

    private void acceptTodoBasedSchedule(SuggestionLearning suggestionLearning) {
        Todo todo = loadLinkedTodo(suggestionLearning);
        Schedule baseSchedule = suggestionLearning.getSchedule();
        ScheduleCategory category = resolveCategory(suggestionLearning, baseSchedule);

        Schedule schedule = Schedule.of(
                suggestionLearning.getUser(),
                category,
                todo.getTitle(),
                suggestionLearning.getStartTime(),
                suggestionLearning.getEndTime(),
                suggestionLearning.getPlaceName(),
                null,
                suggestionLearning.getLatitude(),
                suggestionLearning.getLongitude(),
                null,
                null,
                suggestionLearning.getAiComment()
        );
        scheduleRepository.save(schedule);
        todo.connectSchedule(schedule);
    }


    // 할일 기반 할일 생성
    private void acceptTodoBasedTodo(SuggestionLearning suggestionLearning) {
        Todo todo = loadLinkedTodo(suggestionLearning);
        if (suggestionLearning.getSchedule() == null) {
            throw new CustomException(HomeErrorCode.SUGGESTION_LEARNING_NOT_FOUND);
        }
        todo.connectSchedule(suggestionLearning.getSchedule());
    }

    // 연결된 할일 조회
    private Todo loadLinkedTodo(SuggestionLearning suggestionLearning) {
        if (suggestionLearning.getLinkedTodoId() == null) {
            throw new CustomException(HomeErrorCode.SUGGESTION_LEARNING_NOT_FOUND);
        }
        return todoRepository.findById(suggestionLearning.getLinkedTodoId())
                .orElseThrow(() -> new CustomException(HomeErrorCode.SUGGESTION_LEARNING_NOT_FOUND));
    }

    // 카테고리 결정
    private ScheduleCategory resolveCategory(
            SuggestionLearning suggestionLearning,
            Schedule baseSchedule
    ) {
        if (suggestionLearning.getSuggestedCategoryId() != null) {
            return scheduleCategoryRepository.findByIdAndUser(
                            suggestionLearning.getSuggestedCategoryId(),
                            suggestionLearning.getUser())
                    .orElse(null);
        }
        return baseSchedule != null ? baseSchedule.getScheduleCategory() : null;
    }


    // 동선 연계 할일 편성 처리
    public void acceptRouteDetourTodo(Long userId,Long suggestionId) {
        User user = userRepository.getReferenceById(userId);
        SuggestionLearning suggestionLearning = suggestionLearningRepository.findById(suggestionId)
                .orElseThrow(() -> new CustomException(HomeErrorCode.SUGGESTION_LEARNING_NOT_FOUND));
        if (!suggestionLearning.getUser().getId().equals(user.getId())) {
            throw new CustomException(HomeErrorCode.SUGGESTION_UNAUTHORIZED_ACCESS);
        }

        // 연결된 할일이 없을 경우에만 저장
        Todo linkedTodo = loadLinkedTodo(suggestionLearning);
        if(linkedTodo.getSchedule() == null){
            linkedTodo.connectSchedule(suggestionLearning.getSchedule());
        }

        // 처리 후 제안 학습 삭제
        suggestionLearningRepository.delete(suggestionLearning);
    }

    // 동선 연계 할일 삭제 처리
    public void rejectRouteDetourTodo(Long userId,Long suggestionId) {
        User user = userRepository.getReferenceById(userId);
        SuggestionLearning suggestionLearning = suggestionLearningRepository.findById(suggestionId)
                .orElseThrow(() -> new CustomException(HomeErrorCode.SUGGESTION_LEARNING_NOT_FOUND));
        if (!suggestionLearning.getUser().getId().equals(user.getId())) {
            throw new CustomException(HomeErrorCode.SUGGESTION_UNAUTHORIZED_ACCESS);
        }
        // 처리 후 제안 학습 삭제
        suggestionLearningRepository.delete(suggestionLearning);
    }
}

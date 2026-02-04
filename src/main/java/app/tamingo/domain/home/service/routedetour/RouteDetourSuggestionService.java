package app.tamingo.domain.home.service.routedetour;

import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionPlanType;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.home.repository.SuggestionLearningRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RouteDetourSuggestionService {

    private static final int MAX_ROUTE_DETOUR_RECOMMENDATIONS = 2;
    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final SuggestionLearningRepository suggestionLearningRepository;
    private final RouteTodoRecommendService routeTodoRecommendService;

    public int generateRouteDetourSuggestions(User user, LocalDate targetDate) {
        List<Todo> dailyTodos = todoRepository.findAllTodayTodos(user, targetDate);
        if (dailyTodos.isEmpty()) {
            return 0;
        }

        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        List<Schedule> dailySchedules = scheduleRepository.findAllToDaySchedules(
                user, startOfDay, endOfDay);
        dailySchedules.sort(Comparator.comparing(Schedule::getStartTime));

        if (dailySchedules.size() < 2) {
            return 0;
        }

        int saved = 0;
        for (int i = 0; i < dailySchedules.size() - 1; i++) {
            Schedule current = dailySchedules.get(i);
            Schedule next = dailySchedules.get(i + 1);

            List<RouteTodoRecommendService.RouteDetourCandidate> candidates =
                    routeTodoRecommendService.findRouteDetourCandidates(
                            current, next, dailyTodos);

            for (RouteTodoRecommendService.RouteDetourCandidate candidate : candidates) {
                if (saved >= MAX_ROUTE_DETOUR_RECOMMENDATIONS) {
                    return saved;
                }
                Todo todo = candidate.todo();
                Schedule linkedSchedule = todo.getSchedule() != null ? todo.getSchedule() : next;

                // 이미 동일한 일정-할 일 연계 추천이 존재하는지 확인
                if (suggestionLearningRepository.existsLinkedSuggestion(
                        user,
                        SuggestionType.ROUTE_DETOUR,
                        todo.getId(),
                        linkedSchedule
                )) {
                    continue;
                }

                // AI 코멘트 생성
                String comment = buildAiComment(linkedSchedule, todo);

                SuggestionLearning suggestion = SuggestionLearning.of(
                        user,
                        todo.getTitle(),
                        linkedSchedule,
                        SuggestionType.ROUTE_DETOUR,
                        SuggestionPlanType.TODO_BASED_TODO,
                        todo.getPlaceName(),
                        todo.getLatitude(),
                        todo.getLongitude(),
                        comment,
                        linkedSchedule.getStartTime(),
                        linkedSchedule.getEndTime(),
                        null,
                        candidate.detourMinutes(),
                        todo.getId(),
                        null
                );

                suggestionLearningRepository.save(suggestion);
                saved++;
            }
        }

        if (saved > 0) {
            log.info("[HOME][DETOUR] 경로 연계 추천 저장 완료 userId={}, count={}", user.getId(), saved);
        }

        return saved;
    }

    // ai 커맨트 생성, 일정과 엮인 기존 할 일일 경우 / 일정 가는 길에 해결할 수 있는 할 일일 경우로 구분
    private String buildAiComment(Schedule schedule, Todo todo) {
        String title = schedule != null ? schedule.getTitle() : "일정";
        if (todo.getSchedule() != null) {
            return title + "과 엮인 할 일이에요";
        }
        return title + " 가는 길에 해결할 수 있어요";
    }
}

package app.tamingo.domain.home.service.main;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.home.converter.DailyPlanItemConverter;
import app.tamingo.domain.home.converter.DailyScheduleResponseConverter;
import app.tamingo.domain.home.dto.DailyPlanResponse;
import app.tamingo.domain.home.dto.DailyScheduleResponse;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.home.repository.SuggestionLearningRepository;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyPlanService {

    private final ScheduleRepository scheduleRepository;
    private final SuggestionLearningRepository suggestionLearningRepository;
    private final UserRepository userRepository;
    private final RealTimeScheduleService realTimeScheduleService;
    private final TodoRepository todoRepository;


    // 홈 화면 일정 목록 조회
    @Transactional(readOnly = true)
    public DailyPlanResponse viewDailyPlan(Long userId) {
        User user = userRepository.getReferenceById(userId);
        // 오늘 일정 조회
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        // 일정 조회
        List<Schedule> scheduleList = scheduleRepository.findAllToDaySchedules(user,startOfDay,endOfDay);

        List<DailyPlanResponse.DailyPlanItem> items = new ArrayList<>();

        // 가장 다가오는 일정 판별
        Schedule nextSchedule = scheduleList.stream()
                .filter(s -> s.getStartTime().isAfter(now))
                .findFirst()
                .orElse(null);

        // 일정 → ScheduleItem 변환
        for (Schedule schedule : scheduleList) {
            boolean isNext = nextSchedule != null && schedule.getId().equals(nextSchedule.getId());

            items.add(
                    DailyPlanItemConverter.toScheduleItem(
                            schedule,
                            isNext,
                            now.toLocalTime()
                    )
            );
        }

        // 틈새 일정 추천 조회, 지난 추천을 제외하고 전부 표시
        List<SuggestionLearning> suggestions =
                suggestionLearningRepository.findAllSLFromNow(
                        user,
                        now,
                        SuggestionType.GAP_TIME
                );

        for (SuggestionLearning suggestion : suggestions) {
            items.add(DailyPlanItemConverter.toGapRecommendItem(suggestion));
        }

        // 시간순 정렬
        items.sort(Comparator.comparing(item -> {
            if (item instanceof DailyPlanResponse.ScheduleItem s) {
                return s.getStartTime();
            }
            if (item instanceof DailyPlanResponse.GapRecommendItem g) {
                return g.getTime();
            }
            return LocalTime.MAX;
        }));

        return new DailyPlanResponse(today, items);
    }

    // 일정 상세 조회
    @Transactional(readOnly = true)
    public DailyScheduleResponse viewScheduleDetail(Long userId, Long scheduleId) {
        User user = userRepository.getReferenceById(userId);

        Schedule schedule = scheduleRepository.findByIdAndUser(scheduleId, user)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 길찾기 비활성화 또는 위치 정보 없을 경우에는 추적되지 않음
        if (Boolean.TRUE.equals(schedule.getIsNavigationEnabled())
                && (schedule.getLatitude() == null || schedule.getLongitude() == null)) {
            return new DailyScheduleResponse(null, List.of(), List.of());
        }
        // 일정 상태 계산
        DailyScheduleResponse.ScheduleStatusResponse scheduleStatus =
                realTimeScheduleService.calculateScheduleStatus(schedule);

        // 연결된 할일 조회
        List<Todo> linkedTodos = todoRepository.findAllBySchedule(schedule);
        List<DailyScheduleResponse.LinkedTodoResponse> linkedTodoResponses =
                linkedTodos.stream()
                        .map(todo -> new DailyScheduleResponse.LinkedTodoResponse(
                                todo.getId(),
                                todo.getTitle(),
                                todo.getPlaceName()
                        ))
                        .toList();

        // 동선 연계 추천 조회 (최대 2개)
        List<SuggestionLearning> linkedSuggestions =
                suggestionLearningRepository.findBySchedule(schedule);

        List<DailyScheduleResponse.RecommendationResponse> recommendations =
                linkedSuggestions.stream()
                        .filter(sl -> sl.getSuggestionType() == SuggestionType.ROUTE_DETOUR)
                        .map(DailyScheduleResponseConverter::toRecommendationResponse)
                        .limit(2)
                        .toList();

        return new DailyScheduleResponse(
                scheduleStatus,
                linkedTodoResponses,
                recommendations
        );
    }


}

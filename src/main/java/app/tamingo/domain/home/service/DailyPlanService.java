package app.tamingo.domain.home.service;

import app.tamingo.domain.home.converter.DailyPlanItemConverter;
import app.tamingo.domain.home.dto.DailyPlanResponse;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.home.repository.SuggestionLearningRepository;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.repository.TodoRepository;
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
    private final TodoRepository todoRepository;
    private final SuggestionLearningRepository suggestionLearningRepository;
    private final UserRepository userRepository;


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


}

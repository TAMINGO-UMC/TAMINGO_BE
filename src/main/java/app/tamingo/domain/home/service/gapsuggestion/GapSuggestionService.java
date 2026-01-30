package app.tamingo.domain.home.service.gapsuggestion;

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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GapSuggestionService {

    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;

    private final GapTimeService gapTimeService;
    private final TodoMatchLinkService todoMatchLinkService;

    /**
     * 틈새시간 추천 생성 및 저장
     */
    public void generateGapTimeSuggestions(User user, LocalDate targetDate) {

        // 해당 날짜의 todos와 schedule 조회
        List<Todo> dailyTodos = todoRepository.findAllTodayTodos(user, targetDate);
        LocalDateTime startOfDay = targetDate.atStartOfDay();
        LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
        List<Schedule> dailySchedules = scheduleRepository.findAllToDaySchedules(
                user, startOfDay, endOfDay);

        // 시간순 정렬
        dailySchedules.sort(Comparator.comparing(Schedule::getStartTime));

        // 해당 날짜에 할일이 없거나 연속된 일정이 없을 경우 추천 불가로 처리
        if (dailyTodos.isEmpty() || dailySchedules.size() < 2) {
            log.warn("[HOME][GAP] 틈새 일정 추천 불가 - 할일 또는 연속된 일정 부족 userId={}, date={}",
                    user.getId(), targetDate);
            return;
        }
        // 틈새시간 추출
        List<GapTime> gapTimes = gapTimeService.extractGapTimes(dailySchedules);

        // 틈새시간이 없으면 추천 불가 처리
        if (gapTimes.isEmpty()) {
            log.warn("[HOME][GAP] 틈새 일정 추천 불가 - 추출된 틈새시간 없음 userId={}, date={}",
                    user.getId(), targetDate);
            return;
        }

        List<GapTime> gaps =
                gapTimeService.extractGapTimes(dailySchedules);

        for (GapTime gap : gaps) {
            todoMatchLinkService.matchAndSave(
                    user,
                    gap,
                    dailyTodos,
                    dailySchedules
            );
        }
    }


}

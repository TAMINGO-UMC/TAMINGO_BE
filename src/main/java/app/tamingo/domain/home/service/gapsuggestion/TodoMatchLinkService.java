package app.tamingo.domain.home.service.gapsuggestion;

import app.tamingo.domain.gpt.prompt.common.DataPrompt;
import app.tamingo.domain.gpt.prompt.suggestion.GapSuggestionCommentPrompt;
import app.tamingo.domain.gpt.prompt.suggestion.GapSuggestionPrompt;
import app.tamingo.domain.gpt.service.home.SuggestionGptService;
import app.tamingo.domain.home.dto.TodoScheduleLinkGptRequest;
import app.tamingo.domain.home.dto.TodoScheduleLinkGptResponse;
import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionPlanType;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.home.repository.SuggestionLearningRepository;
import app.tamingo.domain.home.service.geoutil.GeoService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 할일과 일정 연계해서 틈새시간 직접 저장하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoMatchLinkService {

        private final SuggestionLearningRepository suggestionRepository;
        private final SuggestionGptService gptService;
        private final GeoService geoService;
        private final ScheduleCategoryRepository scheduleCategoryRepository;

        /**
         * 틈새시간 하나에 대해 할일 매칭, 추천 저장
         */
        public void matchAndSave(
                        User user,
                        GapTime gap,
                        List<Todo> todos,
                        List<Schedule> schedules) {
                Todo todo = findMatchingTodo(gap, todos);
                if (todo == null)
                        return;


                Optional<GptLinkResult> gptResult = judgeScheduleLinkWithGpt(todo, schedules);

                if (gptResult.isPresent() && !gptResult.get().isLinked()) {
                        return;
                }

                String aiComment = gptResult
                                .map(GptLinkResult::aiComment)
                                .orElse("추천 설명 없음");
                Long categoryId = gptResult
                                .map(GptLinkResult::categoryId)
                                .orElse(null);
                if (gptResult.isEmpty()) {
                        log.debug("[HOME][GAP] GPT 결과 없음. todoId={}, hasLocation={}, hasDuration={}, gapMinutes={}",
                                        todo.getId(), hasLocation(todo), hasDuration(todo), gap.getAvailableMinutes());
                }

                // 1. 장소,소요시간 둘다 있을 경우 gpt comment만 추가로 받아서 저장
                if (hasLocation(todo) && hasDuration(todo)) {
                        if (!isLocationAppropriate(gap, todo)) {
                                log.info("[HOME][GAP] save skipped: location inappropriate todoId={}, gapMinutes={}",
                                                todo.getId(), gap.getAvailableMinutes());
                                return;
                        }

                        saveTodoBasedSchedule(
                                        user, gap, todo, aiComment, categoryId);
                        return;
                }

                // 2. 소요시간만 있을 경우 장소 없이 길찾기하지 않는 일정으로 저장, gpt comment 추가로 받음
                if (!hasLocation(todo) && hasDuration(todo)) {
                        String comment = generateGapCommentWithoutLocation(todo, gap)
                                        .orElse(aiComment);
                        saveTodoWithoutLocation(
                                        user, gap, todo, comment, categoryId);
                }
        }

        /**
         * 틈새시간에 들어갈 수 있는 할일 선택
         */
        protected Todo findMatchingTodo(GapTime gap, List<Todo> todos) {
                Todo durationTodo = todos.stream()
                                .filter(t -> !t.isChecked())
                                .filter(t -> t.getSchedule() == null)
                                .filter(this::hasDuration)
                                .filter(t -> t.getDuration() <= gap.getAvailableMinutes())
                                .sorted(Comparator.comparingInt(Todo::getDuration))
                                .findFirst()
                                .orElse(null);

                return durationTodo;
        }

        /**
         * 위치 적합성 판단
         * 해당 할일과 틈새시간의 이전 일정과 다음 일정 사이의 위치 적합성을 계산
         */
        private boolean isLocationAppropriate(GapTime gap, Todo todo) {

                double fromPrev = geoService.distanceKm(
                                gap.getPreviousSchedule().getLatitude(),
                                gap.getPreviousSchedule().getLongitude(),
                                todo.getLatitude(),
                                todo.getLongitude());

                double toNext = geoService.distanceKm(
                                gap.getNextSchedule().getLatitude(),
                                gap.getNextSchedule().getLongitude(),
                                todo.getLatitude(),
                                todo.getLongitude());

                double totalDetour = fromPrev + toNext;
                double directDistance = gap.getDistanceKm();

                double travelMinutes = totalDetour * 15;
                int remainingMinutes = gap.getAvailableMinutes() - todo.getDuration();

                return totalDetour <= directDistance * 1.5
                                && travelMinutes <= remainingMinutes;
        }

        private Optional<GptLinkResult> judgeScheduleLinkWithGpt(
                        Todo todo,
                        List<Schedule> schedules) {
                List<ScheduleCategory> categories = scheduleCategoryRepository.findAllByUser(todo.getUser());

                List<Schedule> candidates = schedules.stream()
                                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                                .filter(s -> hasLocation(todo)
                                                && geoService.isWithin(
                                                                s.getLatitude(),
                                                                s.getLongitude(),
                                                                todo.getLatitude(),
                                                                todo.getLongitude(),
                                                                0.3))
                                .limit(5)
                                .toList();

                log.debug("GPT link judge: todoId={}, candidates={}", todo.getId(), candidates.size());
                TodoScheduleLinkGptRequest request = TodoScheduleLinkGptRequest.from(candidates, categories);

                TodoScheduleLinkGptResponse response = gptService.getGptResponse(
                                new GapSuggestionPrompt(),
                                new DataPrompt("일정 연계 판단", request.toString()),
                                200);

                if (response == null) {
                        log.debug("GPT link judge: response is null. todoId={}", todo.getId());
                        return Optional.empty();
                }

                if (!response.linked()) {
                        log.debug("GPT link judge: not linked. todoId={}, aiComment={}", todo.getId(),
                                        response.aiComment());
                        return Optional.of(
                                        new GptLinkResult(null, response.aiComment(),
                                                        response.categoryId(),
                                                        response.categoryName()));
                }

                Schedule linkedSchedule = candidates.stream()
                                .filter(s -> s.getId().equals(response.scheduleId()))
                                .findFirst()
                                .orElse(null);

                if (linkedSchedule == null) {
                        log.debug("GPT link judge: linked schedule not found. todoId={}, scheduleId={}",
                                        todo.getId(), response.scheduleId());
                        return Optional.of(
                                        new GptLinkResult(null, response.aiComment(),
                                                        response.categoryId(),
                                                        response.categoryName()));
                }

                return Optional.of(
                                new GptLinkResult(linkedSchedule, response.aiComment(),
                                                response.categoryId(),
                                                response.categoryName()));
        }

        // 장소 없이 ai 커맨트 생성
        private Optional<String> generateGapCommentWithoutLocation(Todo todo, GapTime gap) {
                if (hasLocation(todo)) {
                        return Optional.empty();
                }

                String data = String.join("\n",
                                "todoTitle: " + todo.getTitle(),
                                "durationMinutes: " + todo.getDuration(),
                                "gapStart: " + gap.getStartTime(),
                                "gapEnd: " + gap.getEndTime(),
                                "availableMinutes: " + gap.getAvailableMinutes(),
                                "previousScheduleTitle: " + gap.getPreviousSchedule().getTitle(),
                                "nextScheduleTitle: " + gap.getNextSchedule().getTitle());

                TodoScheduleLinkGptResponse response = gptService.getGptResponse(
                                new GapSuggestionCommentPrompt(),
                                new DataPrompt("틈새 추천 설명 생성", data),
                                120);

                if (response == null || response.aiComment() == null || response.aiComment().isBlank()) {
                        log.debug("[GPT] GPT comment generation returned empty. todoId={}", todo.getId());
                        return Optional.empty();
                }

                return Optional.of(response.aiComment());
        }

        /**
         * 할일 → 일정 변환
         */
        private void saveTodoBasedSchedule(
                        User user, GapTime gap, Todo todo, String aiComment, Long categoryId) {
                LocalDateTime start = gap.getStartTime();
                LocalDateTime end = start.plusMinutes(todo.getDuration());

                suggestionRepository.save(
                                SuggestionLearning.of(
                                                user,
                                                todo.getTitle(),
                                                gap.getPreviousSchedule(),
                                                SuggestionType.GAP_TIME,
                                                SuggestionPlanType.TODO_BASED_SCHEDULE,
                                                todo.getPlaceName(),
                                                todo.getLatitude(),
                                                todo.getLongitude(),
                                                aiComment,
                                                start,
                                                end,
                                                todo.getDuration(),
                                                null,
                                                todo.getId(),
                                                categoryId));
        }

        /**
         * 장소 없는 할일 -> 일정 저장
         */
        private void saveTodoWithoutLocation(
                        User user, GapTime gap, Todo todo, String aiComment, Long categoryId) {
                LocalDateTime start = gap.getStartTime();
                LocalDateTime end = start.plusMinutes(todo.getDuration());

                suggestionRepository.save(
                                SuggestionLearning.of(
                                                user,
                                                todo.getTitle(),
                                                gap.getPreviousSchedule(),
                                                SuggestionType.GAP_TIME,
                                                SuggestionPlanType.TODO_BASED_SCHEDULE,
                                                "위치 미지정",
                                                0,
                                                0,
                                                aiComment,
                                                start,
                                                end,
                                                todo.getDuration(),
                                                null,
                                                todo.getId(),
                                                categoryId));
        }

        private boolean hasLocation(Todo todo) {
                return todo.getLatitude() != null && todo.getLongitude() != null;
        }

        private boolean hasDuration(Todo todo) {
                return todo.getDuration() != null && todo.getDuration() > 0;
        }

        // gpt result
        private record GptLinkResult(
                        Schedule schedule,
                        String aiComment,
                        Long categoryId,
                        String categoryName) {
                boolean isLinked() {
                        return schedule != null;
                }
        }

}

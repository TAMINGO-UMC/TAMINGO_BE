package app.tamingo.domain.schedule.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.calendar.enums.LinkStatus;
import app.tamingo.domain.calendar.repository.ExternalTaskMappingRepository;
import app.tamingo.domain.home.service.realtime.ScheduleInitQueueService;
import app.tamingo.domain.schedule.dto.*;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleAiLog;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleAiLogRepository;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.dto.TodoSummaryResponse;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.service.UserLearningSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;
    private final ScheduleAiLogRepository scheduleAiLogRepository;
    private final ScheduleInitQueueService scheduleInitQueueService;
    private final UserLearningSummaryService userLearningSummaryService;
    private final ExternalTaskMappingRepository externalTaskMappingRepository;

    @Transactional
    public CreateScheduleResponse createSchedule(Long userId, CreateScheduleRequest request){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        ScheduleCategory category = null;
        if (request.scheduleCategoryId() != null) {
            category = scheduleCategoryRepository.findById(request.scheduleCategoryId())
                    .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_NOT_FOUND));
        }

        // 시간 파싱 및 유효성 검사
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startLocalTime = LocalTime.parse(request.startTime(), timeFormatter);
        LocalTime endLocalTime = LocalTime.parse(request.endTime(), timeFormatter);

        if(endLocalTime.isBefore(startLocalTime)){
            throw new CustomException(ScheduleErrorCode.SCHEDULE_PERIOD_INVALID);
        }

        List<Schedule> schedulesToSave = new ArrayList<>();
        LocalDate currentDate = request.scheduleDate();

        // 반복이 없으면 종료일은 시작일과 같음.
        LocalDate endDate = (request.repeatType() == null || request.repeatType() == RepeatType.NONE)
                ? request.scheduleDate()
                : request.repeatEndDate();

        // 종료일이 없거나 시작일보다 빠르면 시작일 하루만 생성
        if (endDate == null || endDate.isBefore(currentDate)) {
            endDate = currentDate;
        }

        // 최대 3년까지만 생성 허용 (무한 루프 방지)
        LocalDate limitDate = request.scheduleDate().plusYears(3);
        if (endDate.isAfter(limitDate)) {
            endDate = limitDate;
        }

        // 여러 개의 Schedule 객체 생성
        while (!currentDate.isAfter(endDate)) {

            LocalDateTime startDateTime = LocalDateTime.of(currentDate, startLocalTime);
            LocalDateTime endDateTime = LocalDateTime.of(currentDate, endLocalTime);

            Schedule schedule = Schedule.of(
                    user,
                    category,
                    request.title(),
                    startDateTime,
                    endDateTime,
                    request.placeName(),
                    request.address(),
                    request.latitude(),
                    request.longitude(),
                    request.repeatType(),     // 반복 타입 그대로 저장
                    request.repeatEndDate(),  // 종료일 그대로 저장
                    request.memo()
            );

            schedulesToSave.add(schedule);

            // 다음 날짜 계산
            if (request.repeatType() == null || request.repeatType() == RepeatType.NONE) {
                break; // 반복 없으면 1회만 수행하고 종료
            } else if (request.repeatType() == RepeatType.DAILY) {
                currentDate = currentDate.plusDays(1);
            } else if (request.repeatType() == RepeatType.WEEKLY) {
                currentDate = currentDate.plusWeeks(1);
            } else if (request.repeatType() == RepeatType.MONTHLY) {
                currentDate = currentDate.plusMonths(1);
            } else if (request.repeatType() == RepeatType.YEARLY) {
                currentDate = currentDate.plusYears(1);
            } else {
                break;
            }
        }

        // 일괄 저장
        List<Schedule> savedSchedules = scheduleRepository.saveAll(schedulesToSave);
        Schedule firstSchedule = savedSchedules.get(0); // 첫 번째 일정을 대표로 사용

        // 일정 시작 20분 전 초기화 큐 등록
        for (Schedule schedule : savedSchedules) {
            scheduleInitQueueService.scheduleInit(
                    schedule.getId(),
                    schedule.getStartTime().minusMinutes(20)
            );
        }

        // 첫 번째 일정에만 할 일 연결
        if (request.linkedTodoIds() != null && !request.linkedTodoIds().isEmpty()) {
            List<Todo> todos = todoRepository.findAllById(request.linkedTodoIds());

            for (Todo todo : todos) {
                // 본인 확인 로직
                if (!todo.getUser().getId().equals(userId)) {
                    throw new CustomException(ErrorCode.INVALID_REQUEST);
                }

                // 할 일의 날짜를 해당 일정의 날짜로 강제 동기화
                todo.connectSchedule(firstSchedule);
            }
        }

        // 첫 번째 일정에 대해서만 AI 로그 저장
        if (request.aiInferenceSource() != null) {
            saveAiLog(user, firstSchedule, category, request);

            userLearningSummaryService.updateAiStats(userId);
        }

        return new CreateScheduleResponse(firstSchedule.getId());
    }

    private void saveAiLog(User user, Schedule schedule, ScheduleCategory finalCategory, CreateScheduleRequest request) {
        String aiPlace = request.aiInferenceSource().aiSuggestedPlaceName();
        String aiCategory = request.aiInferenceSource().aiSuggestedCategoryName();

        // 장소, 카테고리가 없으면 null, 있으면 이름 가져오기
        String userPlace = (schedule.getPlaceName() != null) ? schedule.getPlaceName() : null;
        String userCategory = (finalCategory != null) ? finalCategory.getName() : null;

        // 점수 계산 (장소 일치 50점 + 카테고리 일치 50점 = 최대 100점)
        int score = 0;

        // 장소 비교
        if (Objects.equals(aiPlace, userPlace)) {
            score += 50;
        }

        // 카테고리 비교
        if (Objects.equals(aiCategory, userCategory)) {
            score += 50;
        }

        // 로그 엔티티 생성 및 저장
        ScheduleAiLog log = ScheduleAiLog.of(
                user,
                schedule,
                aiCategory,
                aiPlace,
                userCategory,
                userPlace,
                score
        );

        scheduleAiLogRepository.save(log);
    }

    // 특정 날짜 일정 목록 조회
    public List<ScheduleListResponse> getDailySchedules(Long userId, String dateStr) {

        if (userId == null || dateStr == null || dateStr.isBlank()) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_INVALID_REQUEST);
        }
        LocalDate date;

        // 날짜 파싱 및 유효성 검증
        try {
            date = LocalDate.parse(dateStr); // yyyy-MM-dd 형식이 아니면 예외 발생
        } catch (DateTimeParseException e) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_INVALID_DATE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 해당 날짜의 00:00 ~ 23:59:59 범위 설정
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 조회 및 DTO 변환
        return scheduleRepository
                .findAllByUserAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(user, startOfDay, endOfDay)
                .stream()
                .map(ScheduleListResponse::from)
                .toList();
    }

    // 일정 상세 조회 (Linked + Candidate 분리해서 반환)
    public ScheduleDetailResponse getScheduleDetail(Long userId, Long scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getUser().getId().equals(userId)) {
            // 본인의 일정이 아닐 경우
            throw new CustomException(ScheduleErrorCode.SCHEDULE_NOT_OWNER);
        }

        // Linked Todos
        List<Todo> linkedTodoList = schedule.getTodoList();

        List<TodoSummaryResponse> linkedTodos = linkedTodoList.stream()
                .map(TodoSummaryResponse::from)
                .toList();

        Set<Long> linkedTodoIds = linkedTodoList.stream()
                .map(Todo::getId)
                .collect(Collectors.toSet());

        // Candidate Todos
        LocalDate scheduleDate = schedule.getStartTime().toLocalDate();

        LocalDate startOfWeek = scheduleDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = scheduleDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<TodoSummaryResponse> candidateTodos = todoRepository.findCandidateTodos(
                        userId,
                        startOfWeek,
                        endOfWeek
                ).stream()
                .filter(todo -> !linkedTodoIds.contains(todo.getId())) // 여기서 중복 제거
                .map(TodoSummaryResponse::from)
                .toList();

        return ScheduleDetailResponse.of(schedule, linkedTodos, candidateTodos);
    }

    @Transactional
    public void updateSchedule(Long userId, Long scheduleId, UpdateScheduleRequest request) {

        request.validateTime();

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_NOT_OWNER);
        }

        //외부(Apple)에서 들어온 일정이면, 앱에서 수정하는 순간 UNLINKED 처리
        externalTaskMappingRepository.findByScheduleId(scheduleId)
                .ifPresent(mapping -> {
                    if (mapping.getLinkStatus() == LinkStatus.LINKED) {
                        mapping.unlink(); //이후 /sync에서 schedule 덮어쓰기 스킵
                    }
                });

        ScheduleCategory category = null;
        if (request.scheduleCategoryId() != null) {
            category = scheduleCategoryRepository.findById(request.scheduleCategoryId())
                    .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_NOT_FOUND));
        }

        schedule.update(
                category,
                request.title(),
                request.toStartDateTime(),
                request.toEndDateTime(),
                request.placeName(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.repeatType(),
                request.repeatEndDate(),
                request.memo()
        );

        // 할 일 연결 업데이트
        // 기존 연결 해제
        List<Todo> currentLinkedTodos = schedule.getTodoList();
        for (Todo todo : currentLinkedTodos) {
            todo.disconnectSchedule();
        }

        // 새로 연결
        if (request.linkedTodoIds() != null && !request.linkedTodoIds().isEmpty()) {
            List<Todo> newTodos = todoRepository.findAllById(request.linkedTodoIds());
            for (Todo todo : newTodos) {
                if (!todo.getUser().getId().equals(userId)) {
                    throw new CustomException(ErrorCode.INVALID_REQUEST);
                }
                todo.connectSchedule(schedule);
            }
        }
    }

    // 월간 일정 조회
    public MonthlyScheduleResponse getMonthlySchedules(Long userId, String yearMonth) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        LocalDateTime startOfMonth;
        LocalDateTime endOfMonth;

        try {
            YearMonth ym = YearMonth.parse(yearMonth);
            startOfMonth = ym.atDay(1).atStartOfDay();
            endOfMonth = ym.atEndOfMonth().atTime(LocalTime.MAX);
        } catch (DateTimeParseException e) {
            throw new CustomException(ScheduleErrorCode.SCHEDULE_INVALID_DATE);
        }

        // 해당 월 일정 조회 -> List<ScheduleListResponse> 변환
        List<ScheduleListResponse> schedules = scheduleRepository
                .findAllByUserAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(user, startOfMonth, endOfMonth)
                .stream()
                .map(ScheduleListResponse::from)
                .toList();

        // 카테고리 목록 조회
        List<MonthlyScheduleResponse.CategoryDto> categories = scheduleCategoryRepository.findAllByUser(user)
                .stream()
                .map(MonthlyScheduleResponse.CategoryDto::from)
                .toList();

        // 통합 반환
        return MonthlyScheduleResponse.of(schedules, categories);
    }
}

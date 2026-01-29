package app.tamingo.domain.schedule.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.schedule.dto.CreateScheduleResponse;
import app.tamingo.domain.schedule.dto.CreateScheduleRequest;
import app.tamingo.domain.schedule.dto.ScheduleListResponse;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleAiLog;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleAiLogRepository;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;
    private final ScheduleAiLogRepository scheduleAiLogRepository;

    @Transactional
    public CreateScheduleResponse createSchedule(Long userId, CreateScheduleRequest request){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        ScheduleCategory category = null;
        if (request.scheduleCategoryId() != null) {
            category = scheduleCategoryRepository.findById(request.scheduleCategoryId())
                    .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_CATEGORY_NOT_FOUND));
        }

        Schedule schedule = scheduleRepository.save(request.toEntity(user,category));

        if(request.linkedTodoIds() !=null && !request.linkedTodoIds().isEmpty()){
            List<Todo> todos = todoRepository.findAllById(request.linkedTodoIds());

            for(Todo todo : todos){
                // 일정과 할 일 연결 후 , 할 일의 날짜를 일정의 날짜로 강제 변환
                todo.connectSchedule(schedule);
            }
        }

        if (request.aiInferenceSource() != null) {
            saveAiLog(user, schedule, category, request);
        }

        return new CreateScheduleResponse(schedule.getId());

    }

    private void saveAiLog(
            User user, Schedule schedule, ScheduleCategory finalCategory,
            CreateScheduleRequest request) {

        String aiPlace = request.aiInferenceSource().aiSuggestedPlaceName();
        String aiCategory = request.aiInferenceSource().aiSuggestedCategoryName();

        // 장소, 카테고리 둘 다 없으면 null , 있다면 가져오기
        String userPlace = (schedule.getPlaceName() != null) ? schedule.getPlaceName() : null;
        String userCategory = (finalCategory != null) ? finalCategory.getName() : null;

        // 점수 계산 (장소 일치 50점 + 카테고리 일치 50점 = 최대 100점)
        int score = 0;

        // 장소 비교 (null safe하게 비교)
        if (Objects.equals(aiPlace, userPlace)) {
            score += 50;
        }

        // 카테고리 비교
        if (Objects.equals(aiCategory, userCategory)) {
            score += 50;
        }

        // 로그 엔티티 생성 및 저장
        ScheduleAiLog log = ScheduleAiLog.builder()
                .user(user)
                .schedule(schedule)
                .aiSuggestedPlace(aiPlace)
                .aiSuggestedCategory(aiCategory)
                .userSelectedPlace(userPlace)
                .userSelectedCategory(userCategory)
                .score(score)
                .build();

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
}

package app.tamingo.domain.schedule.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.schedule.dto.CreateScheduleResponse;
import app.tamingo.domain.schedule.dto.CreateScheduleRequest;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.schedule.repository.ScheduleCategoryRepository;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final ScheduleCategoryRepository scheduleCategoryRepository;

    @Transactional
    public CreateScheduleResponse createSchedule(Long userId, CreateScheduleRequest request){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        ScheduleCategory category = null;
        if (request.scheduleCategoryId() != null) {
            category = scheduleCategoryRepository.findById(request.scheduleCategoryId())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        Schedule schedule = scheduleRepository.save(request.toEntity(user,category));

        if(request.linkedTodoIds() !=null && !request.linkedTodoIds().isEmpty()){
            List<Todo> todos = todoRepository.findAllById(request.linkedTodoIds());

            for(Todo todo : todos){
                // 일정과 할 일 연결 후 , 할 일의 날짜를 일정의 날짜로 강제 변환
                todo.connectSchedule(schedule);
            }
        }
        return new CreateScheduleResponse(schedule.getId());

    }
}

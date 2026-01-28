package app.tamingo.domain.schedule.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.gpt.service.schedule.AiScheduleService;
import app.tamingo.domain.schedule.dto.*;
import app.tamingo.domain.schedule.service.PlaceContextService;
import app.tamingo.domain.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final PlaceContextService placeContextService;
    private final AiScheduleService aiScheduleService;

    public record AiInferenceRequest(String title){}

    // 일정 생성 API
    @PostMapping("/create")
    public ApiResponse<CreateScheduleResponse> createSchedule(
            @RequestBody @Valid CreateScheduleRequest request){
        Long userId = 1L;

        CreateScheduleResponse response = scheduleService.createSchedule(userId,request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    // 일정 추천 API
    @PostMapping("/recommend-todos")
    public ApiResponse<RecommendTodoResponse> getRecommendTodos(
            @AuthenticationPrincipal Long userId,
            @RequestBody RecommendTodoRequest request
    ){
        RecommendTodoResponse response = placeContextService.getPlaceContext(
                userId,
                request.placeName(),
                request.latitude(),
                request.longitude()
        );
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    // 3. I 일정 추론 API (제목 -> 장소/할일 추론)
    @PostMapping("/ai-inference")
    public ApiResponse<AiInferenceResponse> inferSchedule(
            @AuthenticationPrincipal Long userId,
            @RequestBody AiInferenceRequest request
    ) {
        AiInferenceResponse response = aiScheduleService.inferSchedule(userId, request.title());
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

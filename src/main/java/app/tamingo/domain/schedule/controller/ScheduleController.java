package app.tamingo.domain.schedule.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.gpt.service.schedule.AiScheduleService;
import app.tamingo.domain.schedule.dto.*;
import app.tamingo.domain.schedule.service.PlaceContextService;
import app.tamingo.domain.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final PlaceContextService placeContextService;
    private final AiScheduleService aiScheduleService;

    // 일정 생성 API
    @PostMapping("/create")
    public ApiResponse<CreateScheduleResponse> createSchedule(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreateScheduleRequest request){

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

    // ai 일정 추론 API (제목 -> 장소/할일 추론)
    @PostMapping("/ai-inference")
    public ApiResponse<AiInferenceResponse> inferSchedule(
            @AuthenticationPrincipal Long userId,
            @RequestBody AiInferenceRequest request
    ) {
        AiInferenceResponse response = aiScheduleService.inferSchedule(userId, request.title());
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    // 일정 목록 조회 api
    @GetMapping
    public ApiResponse<List<ScheduleListResponse>> getDailySchedules(
            @AuthenticationPrincipal Long userId,
            @RequestParam("date") String date
    ) {
        List<ScheduleListResponse> response = scheduleService.getDailySchedules(userId, date);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }


}

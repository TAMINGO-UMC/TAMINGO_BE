package app.tamingo.domain.schedule.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.gpt.service.schedule.AiScheduleService;
import app.tamingo.domain.schedule.dto.*;
import app.tamingo.domain.schedule.service.PlaceContextService;
import app.tamingo.domain.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "일정 관련 API")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final PlaceContextService placeContextService;
    private final AiScheduleService aiScheduleService;

    @Operation(summary = "일정 생성 API",description = "일정 생성")
    @PostMapping("/create")
    public ApiResponse<CreateScheduleResponse> createSchedule(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreateScheduleRequest request){

        CreateScheduleResponse response = scheduleService.createSchedule(userId,request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "사용자 장소 수정 시 할 일 추천 API", description = "사용자가 직접 장소 수정 시 할 일 리스트 추천")
    @PostMapping("/recommend-todos")
    public ApiResponse<RecommendTodoResponse> getRecommendTodos(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RecommendTodoRequest request
    ){
        RecommendTodoResponse response = placeContextService.getPlaceContext(
                userId,
                request.placeName(),
                request.latitude(),
                request.longitude()
        );
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "제목 기반 AI 일정 추론 API",description = "장소&카테고리&자주 가는 장소 추가 여부")
    @PostMapping("/ai-inference")
    public ApiResponse<AiInferenceResponse> inferSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AiInferenceRequest request
    ) {
        AiInferenceResponse response = aiScheduleService.inferSchedule(userId, request.title());
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "특정 일자 일정 목록 API", description = "yyyy-mm-dd 형태로 조회")
    @GetMapping
    public ApiResponse<List<ScheduleListResponse>> getDailySchedules(
            @AuthenticationPrincipal Long userId,
            @RequestParam("date") String date
    ) {
        List<ScheduleListResponse> response = scheduleService.getDailySchedules(userId, date);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "특정 일정 상세 조회 API",description = "특정 일정에 대한 정보 조회")
    @GetMapping("/{scheduleId}")
    public ApiResponse<ScheduleDetailResponse> getScheduleDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("scheduleId") Long scheduleId
    ) {
        ScheduleDetailResponse response = scheduleService.getScheduleDetail(userId, scheduleId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "일정 수정 API",description = "수정된 정보 저장")
    @PutMapping("/{scheduleId}")
    public ApiResponse<String> updateSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody @Valid UpdateScheduleRequest request
    ) {
        scheduleService.updateSchedule(userId, scheduleId, request);
        return ApiResponse.onSuccess("일정이 성공적으로 수정되었습니다.", SuccessCode.OK);
    }

    @Operation(summary = "월간 일정 및 카테고리 조회 API", description = "yyyy-MM 형식으로 요청 시 해당 월의 일정 리스트와 카테고리 정보를 반환합니다.")
    @GetMapping("/calendar")
    public ApiResponse<MonthlyScheduleResponse> getMonthlySchedules(
            @AuthenticationPrincipal Long userId,
            @RequestParam("yearMonth") String yearMonth
    ) {
        MonthlyScheduleResponse response = scheduleService.getMonthlySchedules(userId, yearMonth);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "일정 삭제 API", description = "특정 일정을 삭제합니다. (Soft Delete) 연결된 할 일은 해제되고, AI 학습 통계가 갱신됩니다.")
    @DeleteMapping("/{scheduleId}")
    public ApiResponse<String> deleteSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable("scheduleId") Long scheduleId
    ) {
        scheduleService.deleteSchedule(userId, scheduleId);
        return ApiResponse.onSuccess("일정이 성공적으로 삭제되었습니다.", SuccessCode.OK);
    }
}
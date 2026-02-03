package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.dto.*;
import app.tamingo.domain.home.service.main.DailyPlanService;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "홈 화면 조회 API")
/**
 * 홈 화면 UI 관련 API
 */
public class DailyPlanController {

    private final DailyPlanService dailyPlanService;

    private final RealTimeScheduleService realTimeScheduleService;

    // 오늘의 일정 조회 API
    @GetMapping("/today")
    @Operation(summary = "오늘의 일정 조회", description = "홈 화면에서 오늘 일정 목록과 틈새 추천을 조회합니다.")
    public ApiResponse<DailyPlanResponse> viewTodaySchedules(
            @AuthenticationPrincipal Long userId
            ){
        return ApiResponse.onSuccess(
                dailyPlanService.viewDailyPlan(userId),
                SuccessCode.OK);
    }

    // 현재 일정 상세 조회
    @GetMapping("/schedules/{scheduleId}")
    @Operation(summary = "현재 일정 상세 조회", description = "홈 화면에서 현재 일정 상세 정보를 조회합니다.")
    public ApiResponse<DailyScheduleResponse> viewScheduleDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.onSuccess(dailyPlanService.viewScheduleDetail(userId, scheduleId), SuccessCode.OK);
    }


    // 사용자 출발 처리
    @PostMapping("/route-find/start")
    @Operation(summary = "사용자 출발 처리 및 길찾기 시작", description = "일정 출발 시 출발 처리")
    public ApiResponse<FindRouteResponse> startSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody StartLocationGpsRequest request
            ){
        FindRouteResponse response = realTimeScheduleService.updateDepartureStatus(userId,request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @PostMapping("/route-find/end")
    @Operation(summary = "길찾기 종료 및 도착 처리", description = "길찾기 종료 클릭 시, 도착지에 있다면 시 도착 처리됩니다. 도착지에 없다면 길찾기를 다시 시작합니다.")
    public ApiResponse<FindRouteEndResponse> endSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody StartLocationGpsRequest request
    ){
        LocalDateTime now = LocalDateTime.now();
        FindRouteEndResponse response = realTimeScheduleService.confirmArrivalByEndRouteFind(request,now);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }




}

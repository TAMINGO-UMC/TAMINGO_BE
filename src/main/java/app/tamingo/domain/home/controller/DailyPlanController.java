package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.common.time.VirtualTimeService;
import app.tamingo.domain.home.dto.*;
import app.tamingo.domain.home.service.main.DailyPlanService;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class DailyPlanController implements DailyPlanApi {

    private final DailyPlanService dailyPlanService;

    private final RealTimeScheduleService realTimeScheduleService;
    private final VirtualTimeService virtualTimeService;

    @Override
    public ApiResponse<DailyPlanResponse> viewTodaySchedules(
            @AuthenticationPrincipal Long userId
            ){
        return ApiResponse.onSuccess(
                dailyPlanService.viewDailyPlan(userId),
                SuccessCode.OK);
    }

    @Override
    public ApiResponse<DailyScheduleResponse> viewScheduleDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId
    ) {
        return ApiResponse.onSuccess(dailyPlanService.viewScheduleDetail(userId, scheduleId), SuccessCode.OK);
    }


    @Override
    public ApiResponse<FindRouteResponse> startSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody StartLocationGpsRequest request
            ){
        FindRouteResponse response = realTimeScheduleService.updateDepartureStatus(userId,request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Override
    public ApiResponse<FindRouteEndResponse> endSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody StartLocationGpsRequest request
    ){
        LocalDateTime now = virtualTimeService.now();
        FindRouteEndResponse response = realTimeScheduleService.confirmArrivalByEndRouteFind(request,now);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }



}

package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.dto.DailyPlanResponse;
import app.tamingo.domain.home.service.DailyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "홈 화면 조회 API")
public class DailyPlanController {

    private final DailyPlanService dailyPlanService;

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


}

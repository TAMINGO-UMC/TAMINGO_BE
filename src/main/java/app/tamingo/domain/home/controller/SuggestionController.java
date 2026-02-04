package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.service.main.SuggestionLearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/home/suggestions")
@Tag(name = "홈 화면 틈새시간 추천 API")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionLearningService suggestionLearningService;

    @Operation(summary = "틈새시간 추천 수락", description = "틈새시간 일정/할일 추천 수락 API")
    @PostMapping("/{suggestionId}/accept")
    public ApiResponse<Void> acceptSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "틈새시간 일정 학습 ID") @PathVariable Long suggestionId) {
        suggestionLearningService.acceptSuggestion(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Operation(description = "틈새시간 일정/할일 추천 거절 API", summary = "틈새시간 추천 거절")
    @DeleteMapping("/{suggestionId}/reject")
    public ApiResponse<Void> rejectSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "틈새시간 일정 학습 ID") @PathVariable Long suggestionId) {
        suggestionLearningService.rejectSuggestion(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Operation(summary = "동선 연계 할일 추천 수락",description = "동선 연계 할일 추천을 id로 수락 처리하는 API")
    @PostMapping("/route/{suggestionId}/accept")
    public ApiResponse<Void> acceptRoteDetourSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "경로 연계 추천 ID") @PathVariable Long suggestionId) {
        suggestionLearningService.acceptRouteDetourTodo(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Operation(summary = "동선 연계 할일 추천 거절",description = "동선 연계 할일 추천을 id로 거절 처리하는 API")
    @DeleteMapping("/route/{suggestionId}/reject")
    public ApiResponse<Void> rejectRoteDetourSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "경로 연계 추천 ID") @PathVariable Long suggestionId) {
        suggestionLearningService.rejectRouteDetourTodo(userId,suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }



}

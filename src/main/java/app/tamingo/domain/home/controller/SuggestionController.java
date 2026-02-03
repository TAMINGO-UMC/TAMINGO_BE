package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.service.SuggestionLearningService;
import app.tamingo.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home/suggestions")
@Tag(name = "홈 화면 틈새시간 추천 API")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionLearningService suggestionLearningService;

    @Operation(description = "틈새시간 일정/할일 추천 수락 API", summary = "틈새시간 추천 수락")
    @PostMapping("/{suggestionId}/accept")
    public ApiResponse<Void> acceptSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "틈새시간 일정 학습 ID") @PathVariable Long suggestionId) {
        suggestionLearningService.acceptSuggestion(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Operation(description = "틈새시간 일정/할일 추천 거절 API", summary = "틈새시간 추천 거절")
    @PostMapping("/{suggestionId}/reject")
    public ApiResponse<Void> rejectSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "틈새시간 일정 학습 ID") @PathVariable Long suggestionId) {
        suggestionLearningService.rejectSuggestion(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }
}

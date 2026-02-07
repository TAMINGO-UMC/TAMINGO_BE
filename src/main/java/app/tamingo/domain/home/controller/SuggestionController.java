package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.service.main.SuggestionLearningService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SuggestionController implements SuggestionApi {

    private final SuggestionLearningService suggestionLearningService;

    @Override
    public ApiResponse<Void> acceptSuggestion(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long suggestionId) {
        suggestionLearningService.acceptSuggestion(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Override
    public ApiResponse<Void> rejectSuggestion(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long suggestionId) {
        suggestionLearningService.rejectSuggestion(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Override
    public ApiResponse<Void> acceptRoteDetourSuggestion(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long suggestionId) {
        suggestionLearningService.acceptRouteDetourTodo(userId, suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Override
    public ApiResponse<Void> rejectRoteDetourSuggestion(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long suggestionId) {
        suggestionLearningService.rejectRouteDetourTodo(userId,suggestionId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }



}

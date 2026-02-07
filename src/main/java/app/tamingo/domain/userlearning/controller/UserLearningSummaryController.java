package app.tamingo.domain.userlearning.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.userlearning.dto.UserSummaryResponse;
import app.tamingo.domain.userlearning.service.UserLearningSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserLearningSummaryController implements UserLearningSummaryApi {

    private final UserLearningSummaryService userLearningSummaryService;

    @Override
    public ApiResponse<UserSummaryResponse> viewSummary(@AuthenticationPrincipal Long userId) {
        UserSummaryResponse response = userLearningSummaryService.viewSummary(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Override
    public ApiResponse<Void> resetUserData(@AuthenticationPrincipal Long userId) {
        userLearningSummaryService.resetUserData(userId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }
}

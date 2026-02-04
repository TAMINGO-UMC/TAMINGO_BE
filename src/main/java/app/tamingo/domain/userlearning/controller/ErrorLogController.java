package app.tamingo.domain.userlearning.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.userlearning.dto.ErrorLogResponse;
import app.tamingo.domain.userlearning.dto.ErrorLogSettingResponse;
import app.tamingo.domain.userlearning.service.ErrorLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ErrorLogController implements ErrorLogApi {

    private final ErrorLogService errorLogService;

    @Override
    public ApiResponse<ErrorLogSettingResponse> toggleErrorLogSetting(@AuthenticationPrincipal Long userId) {
        ErrorLogSettingResponse response = errorLogService.setErrorLogSetting(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Override
    public ApiResponse<ErrorLogSettingResponse> viewErrorLogSetting(@AuthenticationPrincipal Long userId) {
        ErrorLogSettingResponse response = errorLogService.viewErrorLogSetting(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Override
    public ApiResponse<List<ErrorLogResponse>> viewRecentErrorLogs(@AuthenticationPrincipal Long userId) {
        List<ErrorLogResponse> response = errorLogService.viewErrorLog(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

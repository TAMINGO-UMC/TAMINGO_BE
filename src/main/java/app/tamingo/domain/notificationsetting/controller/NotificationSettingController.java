package app.tamingo.domain.notificationsetting.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.notificationsetting.dto.NotificationSettingRequest;
import app.tamingo.domain.notificationsetting.dto.NotificationSettingResponse;
import app.tamingo.domain.notificationsetting.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@Tag(name = "알림 설정 API")
@RestController
@RequestMapping("/api/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @Operation(summary = "알림 설정 업데이트", description = "사용자의 알림 설정을 업데이트하고 결과를 반환합니다.")
    @PatchMapping
    public ApiResponse<NotificationSettingResponse> update(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NotificationSettingRequest.UpdateDto dto) {
        NotificationSettingResponse response = notificationSettingService.update(userId, dto);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "알림 설정 조회", description = "현재 로그인한 사용자의 알림 설정 상태를 가져옵니다.")
    @GetMapping
    public ApiResponse<NotificationSettingResponse> getSetting(
            @AuthenticationPrincipal Long userId
    ) {
        NotificationSettingResponse response = notificationSettingService.getSetting(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

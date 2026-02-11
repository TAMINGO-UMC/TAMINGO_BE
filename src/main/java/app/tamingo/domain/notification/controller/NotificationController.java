package app.tamingo.domain.notification.controller;


import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "디바이스 토큰")
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/token")
    @Operation(summary = "FCM 디바이스 토큰 등록", description = "유저의 FCM 토큰을 저장하거나 갱신합니다.")
    public ApiResponse<Void> registerToken(
            @AuthenticationPrincipal Long userId,
            @RequestBody String token) {
        notificationService.registerDeviceToken(userId, token);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }
}

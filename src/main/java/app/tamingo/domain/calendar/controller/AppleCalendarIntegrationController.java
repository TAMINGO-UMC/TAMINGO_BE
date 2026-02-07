package app.tamingo.domain.calendar.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.calendar.dto.AppleCalendarIntegrationStatusResponse;
import app.tamingo.domain.calendar.dto.AppleCalendarIntegrationToggleRequest;
import app.tamingo.domain.calendar.service.AppleCalendarIntegrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 설명: Apple 캘린더 연동 상태 조회/토글 API
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar/apple")
public class AppleCalendarIntegrationController {

    private final AppleCalendarIntegrationService appleCalendarIntegrationService;

    //연동 상태 조회 (앱에서 토글 상태 표시용)
    @GetMapping
    public ApiResponse<AppleCalendarIntegrationStatusResponse> getStatus(
            @AuthenticationPrincipal Long userId
    ) {
        AppleCalendarIntegrationStatusResponse response = appleCalendarIntegrationService.getStatus(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    //연동 토글(PATCH) - ACTIVE/INACTIVE 전환
    @PatchMapping
    public ApiResponse<AppleCalendarIntegrationStatusResponse> toggle(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AppleCalendarIntegrationToggleRequest request
    ) {
        AppleCalendarIntegrationStatusResponse response = appleCalendarIntegrationService.toggle(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

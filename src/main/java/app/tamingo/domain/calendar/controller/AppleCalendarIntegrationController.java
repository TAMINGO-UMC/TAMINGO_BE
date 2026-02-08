package app.tamingo.domain.calendar.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.calendar.dto.AppleCalendarIntegrationStatusResponse;
import app.tamingo.domain.calendar.dto.AppleCalendarIntegrationToggleRequest;
import app.tamingo.domain.calendar.service.AppleCalendarIntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 설명: Apple 캘린더 연동 상태 조회/토글 API
@Tag(name = "애플 캘린더 연동 상태 조회/토글 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar/apple")
public class AppleCalendarIntegrationController {

    private final AppleCalendarIntegrationService appleCalendarIntegrationService;

    //연동 상태 조회 (앱에서 토글 상태 표시용)
    @Operation(
            summary = "애플 캘린더 연동 상태 조회",
            description = "현재 로그인한 사용자의 애플 캘린더 연동 상태(ACTIVE/INACTIVE) 및 토글 표시용 정보를 조회합니다."
    )
    @GetMapping
    public ApiResponse<AppleCalendarIntegrationStatusResponse> getStatus(
            @AuthenticationPrincipal Long userId
    ) {
        AppleCalendarIntegrationStatusResponse response = appleCalendarIntegrationService.getStatus(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    //연동 토글(PATCH) - ACTIVE/INACTIVE 전환
    @PatchMapping
    @Operation(
            summary = "애플 캘린더 연동 토글",
            description = "애플 캘린더 연동 상태를 ACTIVE/INACTIVE로 전환합니다. (토글 ON/OFF)"
    )
    public ApiResponse<AppleCalendarIntegrationStatusResponse> toggle(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AppleCalendarIntegrationToggleRequest request
    ) {
        AppleCalendarIntegrationStatusResponse response = appleCalendarIntegrationService.toggle(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

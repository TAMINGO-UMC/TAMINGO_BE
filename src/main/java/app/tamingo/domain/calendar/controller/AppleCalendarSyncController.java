package app.tamingo.domain.calendar.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.calendar.dto.AppleCalendarSyncRequest;
import app.tamingo.domain.calendar.dto.AppleCalendarSyncResponse;
import app.tamingo.domain.calendar.service.AppleCalendarSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

//iOS가 EventKit 이벤트 리스트를 서버로 보내는 동기화 API
@Tag(name = "iOS(EventKit) 이벤트 리스트 업로드/동기화 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar/apple")
public class AppleCalendarSyncController {

    private final AppleCalendarSyncService appleCalendarSyncService;

    //iOS(EventKit) -> 서버로 이벤트 리스트 업로드(앱 실행/포그라운드/화면 진입 등에서 반복 호출)
    @Operation(
            summary = "애플 캘린더 이벤트 동기화 업로드",
            description = "iOS(EventKit)에서 가져온 이벤트 리스트를 서버로 업로드합니다. " +
                    "앱 실행/포그라운드/화면 진입 시 반복 호출될 수 있으며, 서버는 전달된 이벤트를 기반으로 동기화 처리합니다."
    )
    @PostMapping("/sync")
    public ApiResponse<AppleCalendarSyncResponse> syncFromApple(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AppleCalendarSyncRequest request
    ) {
        AppleCalendarSyncResponse response = appleCalendarSyncService.syncFromApple(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

package app.tamingo.domain.calendar.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.calendar.dto.AppleCalendarSyncRequest;
import app.tamingo.domain.calendar.dto.AppleCalendarSyncResponse;
import app.tamingo.domain.calendar.service.AppleCalendarSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

//iOS가 EventKit 이벤트 리스트를 서버로 보내는 동기화 API
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar/apple")
public class AppleCalendarSyncController {

    private final AppleCalendarSyncService appleCalendarSyncService;

    //iOS(EventKit) -> 서버로 이벤트 리스트 업로드(앱 실행/포그라운드/화면 진입 등에서 반복 호출)
    @PostMapping("/sync")
    public ApiResponse<AppleCalendarSyncResponse> syncFromApple(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AppleCalendarSyncRequest request
    ) {
        AppleCalendarSyncResponse response = appleCalendarSyncService.syncFromApple(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

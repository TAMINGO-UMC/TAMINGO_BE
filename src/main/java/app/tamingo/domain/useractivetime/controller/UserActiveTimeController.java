package app.tamingo.domain.useractivetime.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.useractivetime.dto.UserActiveTimeRequest;
import app.tamingo.domain.useractivetime.dto.UserActiveTimeResponse;
import app.tamingo.domain.useractivetime.service.UserActiveTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "활동 시간 설정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/active-time")
public class UserActiveTimeController {

    private final UserActiveTimeService userActiveTimeService;

    @GetMapping()
    @Operation(summary = "활동 시간 조회", description = "활동 시간을 조회합니다.")
    public ApiResponse<UserActiveTimeResponse> getMyActiveTime(@AuthenticationPrincipal Long userId) {
        UserActiveTimeResponse response = userActiveTimeService.getUserActiveTime(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @PostMapping()
    @Operation(summary = "활동 시간 저장", description = "활동 시간을 설정합니다.")
    public ApiResponse<UserActiveTimeResponse> save(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserActiveTimeRequest request) {
        UserActiveTimeResponse response = userActiveTimeService.save(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.CREATED);
    }
}

package app.tamingo.domain.useractivetime.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.useractivetime.dto.UserActiveTimeRequest;
import app.tamingo.domain.useractivetime.dto.UserActiveTimeResponse;
import app.tamingo.domain.useractivetime.service.UserActiveTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/active-time")
public class UserActiveTimeController {

    private final UserActiveTimeService userActiveTimeService;

    @GetMapping()
    public ApiResponse<UserActiveTimeResponse> getMyActiveTime() {

        // 테스트용
        Long userId = 1L;
        UserActiveTimeResponse response = userActiveTimeService.getUserActiveTime(userId);

        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @PostMapping()
    public ApiResponse<UserActiveTimeResponse> save(@Valid @RequestBody UserActiveTimeRequest request) {
        Long userId = 1L;
        UserActiveTimeResponse response = userActiveTimeService.save(userId, request);

        return ApiResponse.onSuccess(response, SuccessCode.CREATED);
    }
}

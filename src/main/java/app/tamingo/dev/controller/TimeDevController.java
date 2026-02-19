package app.tamingo.dev.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.common.time.VirtualTimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Tag(name = "개발용 시간 제어 API", description = "데모 시뮬레이션을 위한 가상 시간 배속/이동 제어 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/time")
public class TimeDevController {

    private final VirtualTimeService virtualTimeService;

    @Operation(summary = "가상 시간 상태 조회", description = "현재 배속, 누적 이동 분, 실제 시간, 가상 시간을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시간 상태 조회 성공")
    })
    @GetMapping
    public ApiResponse<VirtualTimeService.TimeState> getTimeState() {
        return ApiResponse.onSuccess(virtualTimeService.currentState(), SuccessCode.OK);
    }

    @Operation(summary = "배속 설정", description = "가상 시간 배속을 설정합니다. (허용 범위: 0.1 ~ 1000.0)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배속 설정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_REQUEST")
    })
    @PostMapping("/scale")
    public ApiResponse<VirtualTimeService.TimeState> updateScale(
            @Parameter(description = "적용할 배속 값 (예: 1, 10, 60)")
            @RequestParam("value") double value
    ) {
        return ApiResponse.onSuccess(virtualTimeService.updateScale(value), SuccessCode.OK);
    }

    @Operation(summary = "시간 이동", description = "가상 시간을 분 단위로 즉시 이동합니다. 양수는 미래, 음수는 과거입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "시간 이동 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "INVALID_REQUEST")
    })
    @PostMapping("/shift")
    public ApiResponse<VirtualTimeService.TimeState> shiftMinutes(
            @Parameter(description = "이동할 분 (예: 20, -20)")
            @RequestParam("minutes") long minutes
    ) {
        return ApiResponse.onSuccess(virtualTimeService.shiftMinutes(minutes), SuccessCode.OK);
    }

    @Operation(summary = "이동값 초기화", description = "누적 시간 이동값(shift)만 초기화합니다. 배속(scale)은 유지됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이동값 초기화 성공")
    })
    @PostMapping("/shift/reset")
    public ApiResponse<VirtualTimeService.TimeState> resetShift() {
        return ApiResponse.onSuccess(virtualTimeService.resetShift(), SuccessCode.OK);
    }

    @Operation(summary = "전체 시간 초기화", description = "배속(scale)과 이동값(shift)을 모두 초기화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전체 시간 초기화 성공")
    })
    @PostMapping("/reset")
    public ApiResponse<VirtualTimeService.TimeState> resetAll() {
        return ApiResponse.onSuccess(virtualTimeService.reset(), SuccessCode.OK);
    }
}

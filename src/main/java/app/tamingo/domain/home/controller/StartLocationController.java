package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.dto.Location;
import app.tamingo.domain.home.dto.RealTimeGpsRequest;
import app.tamingo.domain.home.dto.StartLocationGpsRequest;
import app.tamingo.domain.home.dto.StartLocationGpsResponse;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import app.tamingo.domain.home.service.startplace.ScheduleStartSnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
@Tag(name = "출발지 결정 및 실시간 위치 전송 API")
/***
 * 출발지 보정 및 실시간 위치 전송 관련 API
 */
public class StartLocationController {

    private final ScheduleStartSnapshotService scheduleStartSnapshotService;
    private final RealTimeScheduleService realTimeScheduleService;

    @Operation(summary = "Silent GPS Check", description = "알림 1시간 전 GPS 1회 체크로 출발지 보정 여부를 결정합니다.")
    @PostMapping("/silent-gps")
    public ApiResponse<StartLocationGpsResponse> silentGpsCheck(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody StartLocationGpsRequest request
    ) {
        ScheduleStartSnapshotService.StartGpsUpdateResult result =
                scheduleStartSnapshotService.applySilentGpsCheck(
                        userId,
                        request.scheduleId(),
                        new Location(request.latitude(), request.longitude())
                );
        return ApiResponse.onSuccess(toResponse(result), SuccessCode.OK);
    }

    @Operation(summary = "실시간 위치 전송", description = "일정 시작 후 실시간 위치를 전송합니다.")
    @PostMapping("/realtime")
    public ApiResponse<Void> sendRealtimeLocation(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RealTimeGpsRequest request
            ) {
        realTimeScheduleService.updateRealtime(
                userId,
                request.latitude(),
                request.longitude()
        );
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    // 사후 확인 처리
    // TODO : 사용자도 반영
    @Operation(summary = "사후 확인 처리", description = "일정 종료 후 사후 확인을 처리합니다.")
    @PostMapping("/post-check/{scheduleId}")
    public ApiResponse<Void> postCheckLocation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId
    ) {
        realTimeScheduleService.confirmArrivalByPostCheck(userId,scheduleId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }


    // TODO: 추후 분리해야함
    private StartLocationGpsResponse toResponse(
            ScheduleStartSnapshotService.StartGpsUpdateResult result
    ) {
        return new StartLocationGpsResponse(
                result.overridden(),
                result.reason(),
                result.snapshotMinutes(),
                result.gpsMinutes(),
                result.usedStartLat(),
                result.usedStartLng()
        );
    }
}

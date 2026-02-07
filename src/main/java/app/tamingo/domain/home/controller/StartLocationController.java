package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.dto.Location;
import app.tamingo.domain.home.dto.RealTimeGpsRequest;
import app.tamingo.domain.home.dto.StartLocationGpsRequest;
import app.tamingo.domain.home.dto.StartLocationGpsResponse;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import app.tamingo.domain.home.service.startplace.ScheduleStartSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StartLocationController implements StartLocationApi {

    private final ScheduleStartSnapshotService scheduleStartSnapshotService;
    private final RealTimeScheduleService realTimeScheduleService;

    @Override
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
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    @Override
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

    @Override
    public ApiResponse<Void> postCheckLocation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId
    ) {
        realTimeScheduleService.confirmArrivalByPostCheck(userId,scheduleId);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }
}

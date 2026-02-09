package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.domain.home.dto.StartLocationGpsRequest;
import app.tamingo.domain.home.dto.StartLocationGpsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "출발지 결정 및 실시간 위치 전송 API")
@RequestMapping("/api/location")
public interface StartLocationApi {

    @Operation(summary = "Silent GPS Check", description = "알림시간 + 1시간 전 GPS 1회 체크로 출발지 보정 여부를 결정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Silent GPS Check 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "SCHEDULE_NOT_FOUND")
    })
    @PostMapping("/silent-gps")
    ApiResponse<StartLocationGpsResponse> silentGpsCheck(
            @AuthenticationPrincipal Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "알림 1시간 전 GPS 체크를 위한 현재 위치/일정 정보",
                    required = true
            )
            @Valid @RequestBody StartLocationGpsRequest request
    );

    @Operation(summary = "실시간 위치 전송", description = "일정 시작 후 실시간 위치를 전송합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "실시간 위치 전송 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    })
    @PostMapping("/realtime")
    ApiResponse<Void> sendRealtimeLocation(
            @AuthenticationPrincipal Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "실시간 위치 전송용 GPS 좌표",
                    required = true
            )
            @Valid @RequestBody StartLocationGpsRequest request
    );

    @Operation(summary = "사후 확인 처리", description = "일정 종료 후 사후 확인을 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사후 확인 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "SCHEDULE_NOT_FOUND")
    })
    @PostMapping("/post-check/{scheduleId}")
    ApiResponse<Void> postCheckLocation(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "사후 확인할 일정 ID") @PathVariable Long scheduleId
    );
}

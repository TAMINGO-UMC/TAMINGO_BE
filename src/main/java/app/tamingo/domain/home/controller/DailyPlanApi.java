package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.domain.home.dto.DailyPlanResponse;
import app.tamingo.domain.home.dto.DailyScheduleResponse;
import app.tamingo.domain.home.dto.FindRouteEndResponse;
import app.tamingo.domain.home.dto.FindRouteResponse;
import app.tamingo.domain.home.dto.StartLocationGpsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "홈 화면 조회 API")
@RequestMapping("/api/home")
public interface DailyPlanApi {

    @Operation(summary = "오늘의 일정 조회", description = """
            홈 화면에서 오늘 일정 목록과 틈새 추천을 조회합니다.
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "오늘 일정 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    })
    @GetMapping("/today")
    ApiResponse<DailyPlanResponse> viewTodaySchedules(
            @AuthenticationPrincipal Long userId
    );

    @Operation(summary = "현재 일정 상세 조회", description = "홈 화면에서 현재 일정 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND / SCHEDULE_NOT_FOUND")
    })
    @GetMapping("/schedules/{scheduleId}")
    ApiResponse<DailyScheduleResponse> viewScheduleDetail(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 일정 ID") @PathVariable Long scheduleId
    );

    @Operation(summary = "사용자 출발 처리 및 길찾기 시작", description = "길찾기 버튼 클릭 시 출발 처리가 되어있지 않으면 처리를 진행합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "출발 처리 및 길찾기 시작 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "경유지가 너무 많을 경우"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일정"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 도착 처리된 일정")
    })
    @PostMapping("/route-find/start")
    ApiResponse<FindRouteResponse> startSchedule(
            @AuthenticationPrincipal Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "출발 처리 및 길찾기 시작에 필요한 현재 위치/일정 정보",
                    required = true
            )
            @Valid @RequestBody StartLocationGpsRequest request
    );

    @Operation(summary = "길찾기 종료 및 도착 처리", description = "길찾기 종료 클릭 시, 도착지에 있다면 시 도착 처리됩니다. 도착지에 없다면 길찾기를 다시 시작합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "길찾기 종료 및 도착 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "SCHEDULE_NOT_FOUND"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "ALREADY_ARRIVED")
    })
    @PostMapping("/route-find/end")
    ApiResponse<FindRouteEndResponse> endSchedule(
            @AuthenticationPrincipal Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "길찾기 종료 시 현재 위치/일정 정보",
                    required = true
            )
            @Valid @RequestBody StartLocationGpsRequest request
    );
}

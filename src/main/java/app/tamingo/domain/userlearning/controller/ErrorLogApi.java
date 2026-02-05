package app.tamingo.domain.userlearning.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.domain.userlearning.dto.ErrorLogResponse;
import app.tamingo.domain.userlearning.dto.ErrorLogSettingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "오차 로그(개인화 학습) API")
@RequestMapping("/api/personalization")
public interface ErrorLogApi {

    @Operation(summary = "오차 로그 수집 설정 변경", description = "오차 로그 수집 설정을 ON/OFF 합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND / PERSONAL_SETTING_NOT_FOUND")
    })
    @PutMapping("/settings")
    ApiResponse<ErrorLogSettingResponse> toggleErrorLogSetting(
            @AuthenticationPrincipal Long userId
    );

    @Operation(summary = "오차 로그 수집 설정 조회", description = "현재 오차 로그 수집 설정 상태를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND / PERSONAL_SETTING_NOT_FOUND")
    })
    @GetMapping("/settings")
    app.tamingo.common.response.ApiResponse<ErrorLogSettingResponse> viewErrorLogSetting(
            @AuthenticationPrincipal Long userId
    );

    @Operation(summary = "최근 학습 내역 조회", description = "최근 오차 로그 상위 3개를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "오차 로그 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    })
    @GetMapping("/recent")
    ApiResponse<List<ErrorLogResponse>> viewRecentErrorLogs(
            @AuthenticationPrincipal Long userId
    );
}

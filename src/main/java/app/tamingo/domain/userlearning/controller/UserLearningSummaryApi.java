package app.tamingo.domain.userlearning.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.domain.userlearning.dto.UserSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "개인화 학습 요약 API")
@RequestMapping("/api/personalization/summary")
public interface UserLearningSummaryApi {

    @Operation(summary = "개인화 학습 요약 조회", description = "학습 요약(패턴 수, 평균 정확도, FVP 수)을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요약 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    })
    @GetMapping
    ApiResponse<UserSummaryResponse> viewSummary(
            @AuthenticationPrincipal Long userId
    );

    @Operation(summary = "개인화 데이터 리셋", description = "요약/패턴/로그/기록 등 개인화 데이터를 초기화합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리셋 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "USER_NOT_FOUND")
    })
    @DeleteMapping("/reset")
    ApiResponse<Void> resetUserData(
            @AuthenticationPrincipal Long userId
    );
}

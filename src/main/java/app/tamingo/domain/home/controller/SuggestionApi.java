package app.tamingo.domain.home.controller;

import app.tamingo.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "홈 화면 틈새시간 추천 API")
@RequestMapping("/api/home/suggestions")
public interface SuggestionApi {

    @Operation(summary = "틈새시간 추천 수락", description = "틈새시간 일정/할일 추천 수락 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "틈새시간 추천 수락 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "SUGGESTION_UNAUTHORIZED_ACCESS"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "SUGGESTION_LEARNING_NOT_FOUND / SCHEDULE_CATEGORY_NOT_FOUND")
    })
    @PostMapping("/{suggestionId}/accept")
    ApiResponse<Void> acceptSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "틈새시간 일정 학습 ID") @PathVariable Long suggestionId
    );

    @Operation(summary = "틈새시간 추천 거절", description = "틈새시간 일정/할일 추천 거절 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "틈새시간 추천 거절 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "SUGGESTION_UNAUTHORIZED_ACCESS"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "SUGGESTION_LEARNING_NOT_FOUND")
    })
    @DeleteMapping("/{suggestionId}/reject")
    ApiResponse<Void> rejectSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "틈새시간 일정 학습 ID") @PathVariable Long suggestionId
    );

    @Operation(summary = "동선 연계 할일 추천 수락", description = "동선 연계 할일 추천을 id로 수락 처리하는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "동선 연계 할일 추천 수락 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "SUGGESTION_UNAUTHORIZED_ACCESS"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "SUGGESTION_LEARNING_NOT_FOUND")
    })
    @PostMapping("/route/{suggestionId}/accept")
    ApiResponse<Void> acceptRoteDetourSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "경로 연계 추천 ID") @PathVariable Long suggestionId
    );

    @Operation(summary = "동선 연계 할일 추천 거절", description = "동선 연계 할일 추천을 id로 거절 처리하는 API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "동선 연계 할일 추천 거절 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "SUGGESTION_UNAUTHORIZED_ACCESS"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "SUGGESTION_LEARNING_NOT_FOUND")
    })
    @DeleteMapping("/route/{suggestionId}/reject")
    ApiResponse<Void> rejectRoteDetourSuggestion(
            @AuthenticationPrincipal Long userId,
            @Parameter(description = "경로 연계 추천 ID") @PathVariable Long suggestionId
    );
}

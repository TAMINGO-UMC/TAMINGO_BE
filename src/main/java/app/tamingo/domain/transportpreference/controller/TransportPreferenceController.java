package app.tamingo.domain.transportpreference.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.transportpreference.dto.TransportPreferenceResponse;
import app.tamingo.domain.transportpreference.dto.TransportPreferenceUpdateRequest;
import app.tamingo.domain.transportpreference.service.TransportPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "사용자 이동 수단 선호 순위 조회 및 설정 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transport-preferences")
public class TransportPreferenceController {

    private final TransportPreferenceService transportPreferenceService;

    @Operation(
            summary = "이동 수단 선호 순위 조회",
            description = "사용자가 설정한 이동 수단 선호 순위를 조회합니다."
    )
    @GetMapping
    public ApiResponse<TransportPreferenceResponse> getPreferences(
            @AuthenticationPrincipal Long userId
    ) {
        TransportPreferenceResponse response = transportPreferenceService.getPreferences(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(
            summary = "이동 수단 선호 순위 설정",
            description = "이동 수단 1~3순위를 설정하거나 수정합니다."
    )
    @PutMapping
    public ApiResponse<TransportPreferenceResponse> updatePreferences(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid TransportPreferenceUpdateRequest request
    ) {
        TransportPreferenceResponse response = transportPreferenceService.updatePreferences(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}
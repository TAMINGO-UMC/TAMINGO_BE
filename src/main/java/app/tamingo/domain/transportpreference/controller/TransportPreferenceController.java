package app.tamingo.domain.transportpreference.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.transportpreference.dto.TransportPreferenceResponse;
import app.tamingo.domain.transportpreference.dto.TransportPreferenceUpdateRequest;
import app.tamingo.domain.transportpreference.service.TransportPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/me/transport-preferences")
public class TransportPreferenceController {

    private final TransportPreferenceService transportPreferenceService;

    @GetMapping
    public ApiResponse<TransportPreferenceResponse> getPreferences(
            @AuthenticationPrincipal Long userId
    ) {
        TransportPreferenceResponse response = transportPreferenceService.getPreferences(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @PutMapping
    public ApiResponse<TransportPreferenceResponse> updatePreferences(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid TransportPreferenceUpdateRequest request
    ) {
        TransportPreferenceResponse response = transportPreferenceService.updatePreferences(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}
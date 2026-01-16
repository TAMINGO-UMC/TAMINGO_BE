package app.tamingo.domain.onboarding.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.onboarding.dto.OnboardingRequest;
import app.tamingo.domain.onboarding.dto.OnboardingResponse;
import app.tamingo.domain.onboarding.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    // 온보딩 저장 + (임시) 인증 전이므로 userId는 헤더로 받음
    @PostMapping
    public ApiResponse<OnboardingResponse> save(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody @Valid OnboardingRequest request
    ) {
        onboardingService.saveOnboarding(userId, request);
        return ApiResponse.onSuccess(new OnboardingResponse(true), SuccessCode.OK);
    }
}
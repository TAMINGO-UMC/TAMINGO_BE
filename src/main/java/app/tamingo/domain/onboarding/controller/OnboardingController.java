package app.tamingo.domain.onboarding.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.onboarding.dto.OnboardingRequest;
import app.tamingo.domain.onboarding.dto.OnboardingResponse;
import app.tamingo.domain.onboarding.service.OnboardingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "온보딩(사용자 초기 설정 저장) API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    // 온보딩 저장 + (임시) 인증 전이므로 userId는 헤더로 받음
    @Operation(
            summary = "온보딩 정보 저장",
            description = """
                    사용자의 온보딩 정보를 저장합니다.
                    인증 이전 단계이므로 사용자 식별을 위해 X-USER-ID 헤더를 사용합니다.
                    """
    )
    @PostMapping
    public ApiResponse<OnboardingResponse> save(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestBody @Valid OnboardingRequest request
    ) {
        onboardingService.saveOnboarding(userId, request);
        return ApiResponse.onSuccess(new OnboardingResponse(true), SuccessCode.OK);
    }
}
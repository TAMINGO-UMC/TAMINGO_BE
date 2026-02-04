package app.tamingo.domain.auth.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.common.security.BearerTokenResolver;
import app.tamingo.domain.auth.dto.token.TokenRefreshResponse;
import app.tamingo.domain.auth.service.auth.TokenRefreshService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "access token 재발급 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
public class TokenController {

    private static final String REFRESH_HEADER = "X-Refresh-Token";

    private final TokenRefreshService tokenRefreshService;

    @Operation(
            summary = "Access Token 재발급",
            description = "X-Refresh-Token 헤더의 refresh token을 검증하여 새로운 access token을 발급합니다."
    )
    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        TokenRefreshResponse result = tokenRefreshService.refresh(refreshToken);
        return ApiResponse.onSuccess(result, SuccessCode.OK);
    }
}
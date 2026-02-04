package app.tamingo.domain.auth.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.common.security.BearerTokenResolver;
import app.tamingo.domain.auth.dto.token.TokenRefreshResponse;
import app.tamingo.domain.auth.service.auth.TokenRefreshService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/token")
public class TokenController {

    private static final String REFRESH_HEADER = "X-Refresh-Token";

    private final TokenRefreshService tokenRefreshService;

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> refresh(HttpServletRequest request) {
        String refreshToken = request.getHeader(REFRESH_HEADER);

        TokenRefreshResponse result = tokenRefreshService.refresh(refreshToken);
        return ApiResponse.onSuccess(result, SuccessCode.OK);
    }
}
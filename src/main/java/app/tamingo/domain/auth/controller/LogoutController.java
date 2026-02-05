package app.tamingo.domain.auth.controller;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.auth.exception.AuthErrorCode;
import app.tamingo.domain.auth.service.auth.LogoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "로그아웃 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LogoutController {

    private final LogoutService logoutService;

    @Operation(
            summary = "로그아웃",
            description = "refresh token을 기준으로 로그아웃 처리합니다."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        String accessToken = extractBearerOrThrow(authorization);
        logoutService.logout(accessToken, refreshToken);
        return ApiResponse.onSuccess(null, SuccessCode.OK);
    }

    private String extractBearerOrThrow(String header) {
        if (header == null || header.isBlank()) {
            throw new CustomException(AuthErrorCode.TOKEN_MISSING);
        }
        if (!header.startsWith("Bearer ")) {
            throw new CustomException(AuthErrorCode.TOKEN_INVALID);
        }
        return header.substring(7).trim();
    }
}

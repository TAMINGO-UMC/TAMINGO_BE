package app.tamingo.domain.auth.controller;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.auth.dto.login.LoginRequest;
import app.tamingo.domain.auth.dto.login.LoginResponse;
import app.tamingo.domain.auth.service.LoginService;
import app.tamingo.domain.auth.service.LogoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginOutController {

    private final LoginService loginService;
    private final LogoutService logoutService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        LoginService.LoginResult r = loginService.login(req.email(), req.password());
        return ApiResponse.onSuccess(
                new LoginResponse(r.userId(), r.accessToken(), r.refreshToken()),
                SuccessCode.OK
        );
    }

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
            throw new CustomException(ErrorCode.TOKEN_MISSING);
        }
        if (!header.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
        return header.substring(7).trim();
    }
}
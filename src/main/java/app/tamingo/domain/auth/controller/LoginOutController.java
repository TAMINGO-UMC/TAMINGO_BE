package app.tamingo.domain.auth.controller;

import app.tamingo.common.response.ApiResponse;
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
public class LoginController {

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
}
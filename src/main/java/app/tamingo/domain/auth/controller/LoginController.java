package app.tamingo.domain.auth.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.auth.dto.login.KakaoLoginRequest;
import app.tamingo.domain.auth.dto.login.LoginRequest;
import app.tamingo.domain.auth.dto.login.LoginResponse;
import app.tamingo.domain.auth.service.KakaoLoginService;
import app.tamingo.domain.auth.service.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;
    private final KakaoLoginService kakaoLoginService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return ApiResponse.onSuccess(
                loginService.login(req.email(), req.password()),
                SuccessCode.OK
        );
    }

    @PostMapping("/login/kakao")
    public ApiResponse<LoginResponse> kakaoLogin(@RequestBody @Valid KakaoLoginRequest req) {
        return ApiResponse.onSuccess(
                kakaoLoginService.login(req.kakaoAccessToken()),
                SuccessCode.OK
        );
    }
}
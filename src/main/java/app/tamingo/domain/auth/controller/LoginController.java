package app.tamingo.domain.auth.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.auth.kakao.dto.KakaoLoginRequest;
import app.tamingo.domain.auth.dto.login.LoginRequest;
import app.tamingo.domain.auth.dto.login.LoginResponse;
import app.tamingo.domain.auth.service.auth.KakaoLoginService;
import app.tamingo.domain.auth.service.auth.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "이메일 로그인 및 카카오 로그인 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class LoginController {

    private final LoginService loginService;
    private final KakaoLoginService kakaoLoginService;

    @Operation(
            summary = "이메일 로그인",
            description = "이메일/비밀번호로 로그인하고 access/refresh 토큰을 발급합니다."
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest req) {
        return ApiResponse.onSuccess(
                loginService.login(req.email(), req.password()),
                SuccessCode.OK
        );
    }

    @Operation(
            summary = "카카오 로그인",
            description = "카카오 액세스 토큰으로 로그인하고 access/refresh 토큰을 발급합니다."
    )
    @PostMapping("/login/kakao")
    public ApiResponse<LoginResponse> kakaoLogin(@RequestBody @Valid KakaoLoginRequest req) {
        return ApiResponse.onSuccess(
                kakaoLoginService.login(req.kakaoAccessToken()),
                SuccessCode.OK
        );
    }
}
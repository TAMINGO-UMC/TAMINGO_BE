package app.tamingo.domain.auth.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;

import app.tamingo.domain.auth.dto.signup.*;
import app.tamingo.domain.auth.service.auth.SignupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "회원가입 진행 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/signup")
public class SignupController {

    private final SignupService signupService;

    // 회원가입 세션 생성 (약관 동의 저장 + 필수 약관 검증)
    @Operation(
            summary = "회원가입 세션 생성",
            description = "약관 동의를 저장하고 회원가입 세션을 생성합니다. (세션 TTL: 900초)"
    )
    @PostMapping("/sessions")
    public ApiResponse<CreateSignupSessionResponse> createSignupSession(
            @RequestBody CreateSignupSessionRequest request
    ) {
        String sessionId = signupService.createSignupSession(request.terms());
        return ApiResponse.onSuccess(
                new CreateSignupSessionResponse(sessionId, 900L),
                SuccessCode.OK
        );
    }

    // 2. 이메일 인증번호 발송
    @Operation(
            summary = "이메일 인증번호 발송",
            description = "회원가입 세션(X-Signup-Session-Id)을 이용해 이메일로 인증번호를 발송합니다. (TTL: 300초)"
    )
    @PostMapping("/email/code")
    public ApiResponse<SendEmailCodeResponse> sendEmailCode(
            @RequestHeader("X-Signup-Session-Id") String signupSessionId,
            @RequestBody SendEmailCodeRequest request
    ) {
        signupService.sendEmailCode(signupSessionId, request.email());
        return ApiResponse.onSuccess(
                new SendEmailCodeResponse(300L),
                SuccessCode.OK
        );
    }

    // 3. 이메일 인증번호 확인
    @Operation(
            summary = "이메일 인증번호 확인",
            description = "회원가입 세션(X-Signup-Session-Id)과 인증번호를 확인합니다."
    )
    @PostMapping("/email/verify")
    public ApiResponse<VerifyEmailCodeResponse> verifyEmailCode(
            @RequestHeader("X-Signup-Session-Id") String signupSessionId,
            @RequestBody VerifyEmailCodeRequest request
    ) {
        signupService.verifyEmailCode(signupSessionId, request.email(), request.code());
        return ApiResponse.onSuccess(
                new VerifyEmailCodeResponse(true),
                SuccessCode.OK
        );
    }

    // 4. 아이디 생성 (회원가입 완료)
    @Operation(
            summary = "회원가입 완료",
            description = "회원가입을 완료하고 access/refresh 토큰을 발급합니다."
    )
    @PostMapping("/complete")
    public ApiResponse<CompleteSignupResponse> completeSignup(
            @RequestHeader("X-Signup-Session-Id") String signupSessionId,
            @RequestBody CompleteSignupRequest request
    ) {
        CompleteSignupResponse r = signupService.completeSignup(
                signupSessionId,
                request.nickname(),
                request.password()
        );
        return ApiResponse.onSuccess(r, SuccessCode.CREATED
        );
    }
}
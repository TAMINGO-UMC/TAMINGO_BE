package app.tamingo.domain.auth.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;

import app.tamingo.domain.auth.dto.signup.*;
import app.tamingo.domain.auth.service.SignupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/signup")
public class SignupController {

    private final SignupService signupService;

    // 회원가입 세션 생성 (약관 동의 저장 + 필수 약관 검증)
    @PostMapping("/sessions")
    public ApiResponse<CreateSignupSessionResponse> createSignupSession(
            @RequestBody CreateSignupSessionRequest request
    ) {
        String sessionId = signupService.createSignupSession(request.agreedTerms());
        return ApiResponse.onSuccess(
                new CreateSignupSessionResponse(sessionId, 900L),
                SuccessCode.OK
        );
    }

    // 2. 이메일 인증번호 발송
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
    @PostMapping("/complete")
    public ApiResponse<CompleteSignupResponse> completeSignup(
            @RequestHeader("X-Signup-Session-Id") String signupSessionId,
            @RequestBody CompleteSignupRequest request
    ) {
        Long userId = signupService.completeSignup(signupSessionId, request.nickname());
        return ApiResponse.onSuccess(
                new CompleteSignupResponse(userId),
                SuccessCode.CREATED
        );
    }
}
package app.tamingo.domain.user.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "사용자 계정 관리 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인된 사용자의 계정을 탈퇴 처리합니다."
    )
    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal Long userId) {
        userService.withdraw(userId);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }
}
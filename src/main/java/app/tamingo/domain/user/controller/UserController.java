package app.tamingo.domain.user.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal Long userId) {
        userService.withdraw(userId);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }
}
package app.tamingo.domain.mypage.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.mypage.dto.MyPageSummaryResponse;
import app.tamingo.domain.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지 요약 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ApiResponse<MyPageSummaryResponse> getMyPageSummary(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(myPageService.getSummary(userId), SuccessCode.OK);
    }
}

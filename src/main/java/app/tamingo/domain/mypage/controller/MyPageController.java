package app.tamingo.domain.mypage.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.mypage.dto.MyPageSummaryResponse;
import app.tamingo.domain.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지 요약 API
 */
@Tag(name = "마이페이지 API", description = "마이페이지 요약 정보를 조회합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "마이페이지 요약 조회", description = "프로필/주간리포트/카운트/연동상태/설정 요약 정보를 조회합니다.")
    @GetMapping
    public ApiResponse<MyPageSummaryResponse> getMyPageSummary(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(myPageService.getSummary(userId), SuccessCode.OK);
    }
}

package app.tamingo.domain.terms.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.terms.dto.TermsDetailResponse;
import app.tamingo.domain.terms.dto.TermsSummaryResponse;
import app.tamingo.domain.terms.entity.TermsCode;
import app.tamingo.domain.terms.service.TermsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terms")
public class TermsController {

    private final TermsService termsService;

    // 약관 목록 조회
    @GetMapping
    public ApiResponse<List<TermsSummaryResponse>> getTermsList() {
        return ApiResponse.onSuccess(
                termsService.getTermsList(),
                SuccessCode.OK
        );
    }

    // 약관 개별 조회
    @GetMapping("/{code}")
    public ApiResponse<TermsDetailResponse> getTermsDetail(
            @PathVariable TermsCode code
    ) {
        return ApiResponse.onSuccess(
                termsService.getTermsDetail(code),
                SuccessCode.OK
        );
    }
}
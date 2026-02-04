package app.tamingo.domain.terms.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.terms.dto.TermsDetailResponse;
import app.tamingo.domain.terms.dto.TermsSummaryResponse;
import app.tamingo.domain.terms.entity.TermsCode;
import app.tamingo.domain.terms.service.TermsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "서비스 이용 약관 조회 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terms")
public class TermsController {

    private final TermsService termsService;

    // 약관 목록 조회
    @Operation(
            summary = "약관 목록 조회",
            description = "서비스에서 제공하는 전체 약관 목록을 조회합니다."
    )
    @GetMapping
    public ApiResponse<List<TermsSummaryResponse>> getTermsList() {
        return ApiResponse.onSuccess(
                termsService.getTermsList(),
                SuccessCode.OK
        );
    }

    // 약관 개별 조회
    @Operation(
            summary = "약관 상세 조회",
            description = "약관 코드로 개별 약관의 상세 내용을 조회합니다."
    )
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
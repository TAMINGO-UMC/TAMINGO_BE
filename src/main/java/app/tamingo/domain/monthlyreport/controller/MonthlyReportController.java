package app.tamingo.domain.monthlyreport.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.monthlyreport.dto.MonthlyReportResponse;
import app.tamingo.domain.monthlyreport.service.MonthlyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports/monthly")
public class MonthlyReportController {

    private final MonthlyReportService monthlyReportService;

    // 예: GET /api/reports/monthly?yearMonth=2026-01
    @GetMapping
    public ApiResponse<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal Long userId,
            @RequestParam("yearMonth") String yearMonth
    ) {
        MonthlyReportResponse response = monthlyReportService.getMonthlyReport(userId, yearMonth);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

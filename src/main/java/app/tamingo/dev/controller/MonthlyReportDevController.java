package app.tamingo.dev.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.monthlyreport.batch.MonthlyReportBatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;


@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/reports")
public class MonthlyReportDevController {

    private final MonthlyReportBatchService monthlyReportBatchService;

    // 예: POST /api/dev/reports/monthly?yearMonth=2026-01
    @PostMapping("/monthly")
    public ApiResponse<LocalDate> generateMonthly(@RequestParam("yearMonth") String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        monthlyReportBatchService.generateMonthlyReports(ym);
        return ApiResponse.onSuccess(ym.atDay(1), SuccessCode.OK);
    }
}

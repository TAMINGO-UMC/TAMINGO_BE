package app.tamingo.dev.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.monthlyreport.batch.MonthlyReportBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;

@Tag(name = "개발용 월간리포트 스케줄러 API", description = "데모 시뮬레이션을 위한 스케줄러 실행")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/reports")
public class MonthlyReportDevController {

    private final MonthlyReportBatchService monthlyReportBatchService;

    // 예: POST /api/dev/reports/monthly?yearMonth=2026-01
    @Operation(summary = "스케줄러 실행", description = "전체 사용자 월간리포트 스케줄링을 합니다.")
    @PostMapping("/monthly")
    public ApiResponse<LocalDate> generateMonthly(@RequestParam("yearMonth") String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        monthlyReportBatchService.generateMonthlyReports(ym);
        return ApiResponse.onSuccess(ym.atDay(1), SuccessCode.OK);
    }
}

package app.tamingo.dev.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.weeklyreport.batch.WeeklyReportBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Tag(name = "개발용 주간리포트 스케줄러 API", description = "데모 시뮬레이션을 위한 스케줄러 실행")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/reports")
public class WeeklyReportDevController {

    private final WeeklyReportBatchService weeklyReportBatchService;

    // 예: POST /api/dev/reports/weekly?weekStartDate=2026-01-26
    @Operation(summary = "스케줄러 실행", description = "전체 사용자 주간리포트 스케줄링을 합니다.")
    @PostMapping("/weekly")
    public ApiResponse<LocalDate> generateWeekly(
            @RequestParam("weekStartDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        LocalDate monday = weekStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        weeklyReportBatchService.generateWeeklyReports(monday);
        return ApiResponse.onSuccess(monday, SuccessCode.OK);
    }
}

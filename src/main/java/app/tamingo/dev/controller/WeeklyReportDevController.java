package app.tamingo.dev.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.weeklyreport.batch.WeeklyReportBatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Tag(name = "개발용 dev api 실사용x")
@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev/reports")
public class WeeklyReportDevController {

    private final WeeklyReportBatchService weeklyReportBatchService;

    // 예: POST /api/dev/reports/weekly?weekStartDate=2026-01-26
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

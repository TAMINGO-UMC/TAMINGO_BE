package app.tamingo.domain.weeklyreport.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.weeklyreport.dto.WeeklyReportResponse;
import app.tamingo.domain.weeklyreport.service.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports/weekly")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @GetMapping
    public ApiResponse<WeeklyReportResponse> getWeeklyReport(
            @AuthenticationPrincipal Long userId,
            @RequestParam("weekStartDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate
    ) {
        LocalDate monday = weekStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        WeeklyReportResponse response = weeklyReportService.getWeeklyReport(userId, monday);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

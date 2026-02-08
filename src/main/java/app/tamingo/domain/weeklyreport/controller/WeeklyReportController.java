package app.tamingo.domain.weeklyreport.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.weeklyreport.dto.WeeklyReportResponse;
import app.tamingo.domain.weeklyreport.service.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Tag(name = "주간리포트 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports/weekly")
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    @Operation(
            summary = "주간 리포트 조회",
            description = "weekStartDate(YYYY-MM-DD)가 속한 주의 주간 리포트를 조회합니다. 서버에서 해당 날짜를 그 주의 월요일로 자동 보정합니다."
    )
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

package app.tamingo.test.controller;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.service.startplace.ScheduleStartSnapshotService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/home/startplace")
public class StartPlaceTestController {

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final ScheduleRepository scheduleRepository;
    private final ScheduleStartSnapshotService scheduleStartSnapshotService;

    @PostMapping("/{scheduleId}/decide")
    public ApiResponse<StartPlaceTestResponse> decideStartPlace(
            @PathVariable Long scheduleId
    ) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        boolean created = scheduleStartSnapshotService.createSnapshotForSchedule(
                schedule,
                LocalDateTime.now(TARGET_ZONE)
        );

        ScheduleStartSnapshotService.StartLocationSnapshotInfo snapshotInfo =
                scheduleStartSnapshotService.findSnapshotLocation(schedule);

        return ApiResponse.onSuccess(
                new StartPlaceTestResponse(created, snapshotInfo),
                SuccessCode.OK
        );
    }

    public record StartPlaceTestResponse(
            boolean created,
            ScheduleStartSnapshotService.StartLocationSnapshotInfo snapshot
    ) {
    }
}

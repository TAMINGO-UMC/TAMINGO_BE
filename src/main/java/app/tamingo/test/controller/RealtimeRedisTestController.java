package app.tamingo.test.controller;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.dto.DailyScheduleResponse;
import app.tamingo.domain.home.redis.RealtimeActiveSchedule;
import app.tamingo.domain.home.redis.RealtimeSchedule;
import app.tamingo.domain.home.redis.RealtimeScheduleArrivalCheck;
import app.tamingo.domain.home.service.realtime.RealTimeScheduleService;
import app.tamingo.domain.home.service.realtime.RealtimeScheduleRedisService;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.exception.ScheduleErrorCode;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.test.initializer.TestDataInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/home/realtime")
public class RealtimeRedisTestController {

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final RealTimeScheduleService realTimeScheduleService;
    private final RealtimeScheduleRedisService realtimeScheduleRedisService;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;

    @PostMapping("/init")
    public ApiResponse<RealtimeSchedule> initializeRealtime(
            @RequestParam("scheduleId") Long scheduleId
    ) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        realTimeScheduleService.initializeRealtimeFromSnapshot(
                schedule.getId(),
                LocalDateTime.now(TARGET_ZONE)
        );

        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(scheduleId);
        return ApiResponse.onSuccess(realtime, SuccessCode.OK);
    }

    @PostMapping("/gps")
    public ApiResponse<RealtimeGpsTestResponse> updateGps(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam("lat") double lat,
            @RequestParam("lng") double lng
    ) {
        User user = resolveUser(userId);
        realTimeScheduleService.updateRealtime(user.getId(), lat, lng);

        RealtimeActiveSchedule active = realtimeScheduleRedisService.findActiveSchedule(user.getId());
        RealtimeSchedule realtime = active != null
                ? realtimeScheduleRedisService.findScheduleStatus(active.getScheduleId())
                : null;
        DailyScheduleResponse.ScheduleStatusResponse status =
                realtimeScheduleRedisService.toStatusResponse(realtime);

        return ApiResponse.onSuccess(
                new RealtimeGpsTestResponse(active, realtime, status),
                SuccessCode.OK
        );
    }

    @GetMapping("/status")
    public ApiResponse<RealtimeStatusResponse> getRealtimeStatus(
            @RequestParam("scheduleId") Long scheduleId
    ) {
        RealtimeSchedule realtime = realtimeScheduleRedisService.findScheduleStatus(scheduleId);
        DailyScheduleResponse.ScheduleStatusResponse status =
                realtimeScheduleRedisService.toStatusResponse(realtime);
        RealtimeScheduleArrivalCheck arrivalCheck =
                realtimeScheduleRedisService.findArrivalCheck(scheduleId);

        return ApiResponse.onSuccess(
                new RealtimeStatusResponse(realtime, status, arrivalCheck),
                SuccessCode.OK
        );
    }

    @GetMapping("/active")
    public ApiResponse<RealtimeActiveSchedule> getActiveSchedule(
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        User user = resolveUser(userId);
        RealtimeActiveSchedule active = realtimeScheduleRedisService.findActiveSchedule(user.getId());
        return ApiResponse.onSuccess(active, SuccessCode.OK);
    }

    @DeleteMapping("/reset")
    public ApiResponse<RealtimeResetResponse> resetRealtime(
            @RequestParam("scheduleId") Long scheduleId,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        User user = resolveUser(userId);
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        realtimeScheduleRedisService.deleteScheduleStatus(scheduleId);
        realtimeScheduleRedisService.deleteArrivalCheck(scheduleId);

        RealtimeActiveSchedule active = realtimeScheduleRedisService.findActiveSchedule(user.getId());
        if (active != null && scheduleId.equals(active.getScheduleId())) {
            realtimeScheduleRedisService.deleteActiveSchedule(user.getId());
        }

        return ApiResponse.onSuccess(
                new RealtimeResetResponse(scheduleId, user.getId()),
                SuccessCode.OK
        );
    }

    private User resolveUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        }
        return userRepository.findByEmail(TestDataInitializer.TEST_EMAIL)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    public record RealtimeGpsTestResponse(
            RealtimeActiveSchedule activeSchedule,
            RealtimeSchedule realtimeSchedule,
            DailyScheduleResponse.ScheduleStatusResponse status
    ) {
    }

    public record RealtimeStatusResponse(
            RealtimeSchedule realtimeSchedule,
            DailyScheduleResponse.ScheduleStatusResponse status,
            RealtimeScheduleArrivalCheck arrivalCheck
    ) {
    }

    public record RealtimeResetResponse(
            Long scheduleId,
            Long userId
    ) {
    }
}

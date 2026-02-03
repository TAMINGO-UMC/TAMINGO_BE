package app.tamingo.domain.schedule.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.schedule.dto.ScheduleCategoryResponse;
import app.tamingo.domain.schedule.dto.ScheduleCategoryUpsertRequest;
import app.tamingo.domain.schedule.service.ScheduleCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule-categories")
public class ScheduleCategoryController {

    private final ScheduleCategoryService scheduleCategoryService;

    /**
     * 스케줄 카테고리 목록 조회
     */
    @GetMapping
    public ApiResponse<List<ScheduleCategoryResponse>> getScheduleCategories(
            @AuthenticationPrincipal Long userId
    ) {
        //리스트 조회
        List<ScheduleCategoryResponse> response = scheduleCategoryService.list(userId);

        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    /**
     * 스케줄 카테고리 생성
     */
    @PostMapping
    public ApiResponse<ScheduleCategoryResponse> createScheduleCategory(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ScheduleCategoryUpsertRequest request
    ) {
        //카테고리 생성
        ScheduleCategoryResponse response = scheduleCategoryService.create(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.CREATED);
    }

    /**
     * 스케줄 카테고리 수정
     */
    @PatchMapping("/{categoryId}")
    public ApiResponse<ScheduleCategoryResponse> updateScheduleCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long categoryId,
            @Valid @RequestBody ScheduleCategoryUpsertRequest request
    ) {
        //카테고리 수정
        ScheduleCategoryResponse response = scheduleCategoryService.update(userId, categoryId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    /**
     * 스케줄 카테고리 삭제
     */
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteScheduleCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long categoryId
    ) {
        //카테고리 삭제
        scheduleCategoryService.delete(userId, categoryId);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }
}

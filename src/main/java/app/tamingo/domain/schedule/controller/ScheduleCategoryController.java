package app.tamingo.domain.schedule.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.schedule.dto.ScheduleCategoryResponse;
import app.tamingo.domain.schedule.dto.ScheduleCategoryUpsertRequest;
import app.tamingo.domain.schedule.service.ScheduleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "일정 카테고리 조회,생성,수정,삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule-categories")
public class ScheduleCategoryController {

    private final ScheduleCategoryService scheduleCategoryService;

    /**
     * 스케줄 카테고리 목록 조회
     */
    @Operation(summary = "스케줄 카테고리 목록 조회", description = "사용자의 스케줄 카테고리 목록을 조회합니다.")
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
    @Operation(summary = "스케줄 카테고리 생성", description = "스케줄 카테고리를 생성합니다.")
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
    @Operation(summary = "스케줄 카테고리 수정", description = "categoryId에 해당하는 스케줄 카테고리를 수정합니다.")
    @PatchMapping("/{categoryId}")
    public ApiResponse<ScheduleCategoryResponse> updateScheduleCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody ScheduleCategoryUpsertRequest request
    ) {
        //카테고리 수정
        ScheduleCategoryResponse response = scheduleCategoryService.update(userId, categoryId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    /**
     * 스케줄 카테고리 삭제
     */
    @Operation(summary = "스케줄 카테고리 삭제", description = "categoryId에 해당하는 스케줄 카테고리를 삭제합니다.")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteScheduleCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable("categoryId") Long categoryId
    ) {
        //카테고리 삭제
        scheduleCategoryService.delete(userId, categoryId);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }
}

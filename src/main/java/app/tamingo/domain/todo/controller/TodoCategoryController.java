package app.tamingo.domain.todo.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.todo.dto.TodoCategoryResponse;
import app.tamingo.domain.todo.dto.TodoCategoryUpsertRequest;
import app.tamingo.domain.todo.service.TodoCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todo-categories")
public class TodoCategoryController {

    private final TodoCategoryService todoCategoryService;

    /**
     * 할일 카테고리 목록 조회
     */
    @Operation(summary = "할일 카테고리 목록 조회", description = "사용자의 할일 카테고리 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<TodoCategoryResponse>> getTodoCategories(
            @AuthenticationPrincipal Long userId
    ) {
        //리스트 조회
        List<TodoCategoryResponse> response = todoCategoryService.list(userId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    /**
     * 할일 카테고리 생성
     */
    @Operation(summary = "할일 카테고리 생성", description = "할일 카테고리를 생성합니다.")
    @PostMapping
    public ApiResponse<TodoCategoryResponse> createTodoCategory(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TodoCategoryUpsertRequest request
    ) {
        //할일 카테고리 서비스 호출
        TodoCategoryResponse response = todoCategoryService.create(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.CREATED);
    }

    /**
     * 할일 카테고리 수정
     */
    @Operation(summary = "할일 카테고리 수정", description = "categoryId에 해당하는 할일 카테고리를 수정합니다.")
    @PatchMapping("/{categoryId}")
    public ApiResponse<TodoCategoryResponse> updateTodoCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable("categoryId") Long categoryId,
            @Valid @RequestBody TodoCategoryUpsertRequest request
    ) {
        //할일 카테고리 서비스 호출
        TodoCategoryResponse response = todoCategoryService.update(userId, categoryId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    /**
     * 할일 카테고리 삭제
     */
    @Operation(summary = "할일 카테고리 삭제", description = "categoryId에 해당하는 할일 카테고리를 삭제합니다.")
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteTodoCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable("categoryId") Long categoryId
    ) {
        //할일 카테고리 서비스 호출
        todoCategoryService.delete(userId, categoryId);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }
}

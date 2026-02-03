package app.tamingo.domain.todo.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.todo.dto.TodoCategoryResponse;
import app.tamingo.domain.todo.dto.TodoCategoryUpsertRequest;
import app.tamingo.domain.todo.service.TodoCategoryService;
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

package app.tamingo.domain.todo.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.gpt.service.todo.AiTodoService;
import app.tamingo.domain.todo.dto.*;
import app.tamingo.domain.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todos")
@Tag(name = "Todo API", description = "할 일 관련 API")
public class TodoController {

    private final TodoService todoService;
    private final AiTodoService aiTodoService;

    @Operation(summary = "할 일 생성 API", description = "AI 추론 결과를 포함하여 할 일을 생성합니다. 초기 AI 점수는 100점으로 기록됩니다.")
    @PostMapping
    public ApiResponse<CreateTodoResponse> createTodo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateTodoRequest request
    ) {
        CreateTodoResponse response = todoService.create(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "할 일 AI 추론 API", description = "할 일 제목을 입력받아 카테고리, 장소, 소요시간을 추론합니다.")
    @PostMapping("/ai-inference")
    public ApiResponse<AiTodoInferenceResponse> inferTodo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AiTodoInferenceRequest request
    ) {
        AiTodoInferenceResponse response = aiTodoService.inferTodo(userId, request.title());
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "할 일 상세 조회 API", description = "할 일의 상세 정보와 연결 가능한 일정 후보(위치 기반, 주간 일정)를 조회합니다.")
    @GetMapping("/{todoId}")
    public ApiResponse<TodoDetailResponse> getTodoDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId
    ) {
        TodoDetailResponse response = todoService.getTodoDetail(userId, todoId);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @Operation(summary = "할 일 수정 API", description = "할 일의 정보를 수정합니다. 일정 연결/해제 및 AI 로그 점수 갱신이 수행됩니다.")
    @PutMapping("/{todoId}")
    public ApiResponse<String> updateTodo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody UpdateTodoRequest request
    ) {
        todoService.update(userId, todoId, request);
        return ApiResponse.onSuccess("할 일이 성공적으로 수정되었습니다.", SuccessCode.OK);
    }

    @Operation(summary = "할 일 체크/해제 API", description = "할 일의 완료 여부를 변경합니다.")
    @PatchMapping("/{todoId}/check")
    public ApiResponse<String> updateTodoCheck(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long todoId,
            @Valid @RequestBody TodoCheckRequest request
    ) {
        todoService.updateCheckStatus(userId, todoId, request.isChecked());
        return ApiResponse.onSuccess("상태가 변경되었습니다.", SuccessCode.OK);
    }

    @Operation(summary = "할 일 장소 수정 시 일정 추천 API", description = "입력된 장소 주변의 일정과 향후 7일간의 일정을 추천합니다.")
    @PostMapping("/recommend-schedules")
    public ApiResponse<RecommendScheduleResponse> recommendSchedules(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RecommendScheduleRequest request
    ) {
        RecommendScheduleResponse response = todoService.recommendSchedules(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }
}

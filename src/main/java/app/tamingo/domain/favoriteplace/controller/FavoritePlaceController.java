package app.tamingo.domain.favoriteplace.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceRequest;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceResponse;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceSimpleResponse;
import app.tamingo.domain.favoriteplace.service.FavoritePlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "자주 가는 장소 API", description = "자주 가는 장소 등록, 수정, 삭제 및 조회")
@RequestMapping("/api/favorite-places")
public class FavoritePlaceController {
    private final FavoritePlaceService favoritePlaceService;

    // 자주 가는 장소 등록
    @PostMapping
    @Operation(summary = "자주 가는 장소 등록", description = "새로운 장소를 등록합니다.")
    public ApiResponse<Long> save(@AuthenticationPrincipal Long userId,
            @RequestBody @Valid FavoritePlaceRequest.SaveDto request) {
            Long response = favoritePlaceService.save(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.CREATED);
    }

    // 자주 가는 장소 목록 조회
    @GetMapping
    @Operation(summary = "자주 가는 장소 목록 조회", description = "자주 가는 장소 목록을 조회합니다.")
    public ApiResponse<List<FavoritePlaceResponse>> findAll(@AuthenticationPrincipal Long userId) {
        List<FavoritePlaceResponse> places = favoritePlaceService.findAll(userId);
        return ApiResponse.onSuccess(places, SuccessCode.OK);
    }

    // 자주 가는 장소 수정
    @PatchMapping("/{placeId}")
    @Operation(summary = "자주 가는 장소 수정", description = "자주 가는 장소를 수정합니다.")
    public ApiResponse<Void> update(
            @PathVariable Long placeId,
            @RequestBody @Valid FavoritePlaceRequest.UpdateDto request) {
        favoritePlaceService.update(placeId, request);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }

    // 자주 가는 장소 삭제
    @DeleteMapping("/{placeId}")
    @Operation(summary = "자주 가는 장소 삭제", description = "자주 가는 장소를 삭제합니다.")
    public ApiResponse<Void> delete(@PathVariable Long placeId) {
        favoritePlaceService.delete(placeId);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }

    // 일정,할일에서 자주 가는 장소 목록 조회
    @Operation(summary = "일정, 할 일 자주 가는 장소 조회 API",description = "일정, 할 일에서 사용")
    @GetMapping("/simple")
    public ApiResponse<List<FavoritePlaceSimpleResponse>> findAllSimple(
            @AuthenticationPrincipal Long userId
    ) {
        List<FavoritePlaceSimpleResponse> places = favoritePlaceService.findAllSimple(userId);
        return ApiResponse.onSuccess(places, SuccessCode.OK);
    }
}

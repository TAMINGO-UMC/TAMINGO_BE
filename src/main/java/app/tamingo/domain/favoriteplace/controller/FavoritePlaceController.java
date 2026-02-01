package app.tamingo.domain.favoriteplace.controller;

import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceRequest;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceResponse;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceSimpleResponse;
import app.tamingo.domain.favoriteplace.service.FavoritePlaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorite-places")
public class FavoritePlaceController {
    private final FavoritePlaceService favoritePlaceService;

    // 자주 가는 장소 등록
    @PostMapping
    public ApiResponse<Long> save(
            @RequestBody @Valid FavoritePlaceRequest.SaveDto request) {
            // 테스트용 유저 - 추후 수정 예정
            Long userId = 1L;
            Long response = favoritePlaceService.save(userId, request);
        return ApiResponse.onSuccess(response, SuccessCode.CREATED);
    }

    // 자주 가는 장소 목록 조회
    @GetMapping
    public ApiResponse<List<FavoritePlaceResponse>> findAll() {
            // 테스트용 유저 - 추후 수정 예정
            Long userId = 1L;
        List<FavoritePlaceResponse> places = favoritePlaceService.findAll(userId);
        return ApiResponse.onSuccess(places, SuccessCode.OK);
    }

    // 자주 가는 장소 수정
    @PatchMapping("/{placeId}")
    public ApiResponse<Void> update(
            @PathVariable Long placeId,
            @RequestBody @Valid FavoritePlaceRequest.UpdateDto request) {
        favoritePlaceService.update(placeId, request);
        return ApiResponse.onSuccess(null, SuccessCode.NO_CONTENT);
    }

    // 자주 가는 장소 삭제
    @DeleteMapping("/{placeId}")
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

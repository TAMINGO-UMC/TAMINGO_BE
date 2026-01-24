package app.tamingo.domain.favoriteplace.controller;

import app.tamingo.domain.favoriteplace.dto.FavoritePlaceRequest;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceResponse;
import app.tamingo.domain.favoriteplace.service.FavoritePlaceService;
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
    public ResponseEntity<Long> save(
            @RequestBody @Valid FavoritePlaceRequest.SaveDto request) {
            // 테스트용 유저 - 추후 수정 예정
            Long userId = 1L;
        return ResponseEntity.ok(favoritePlaceService.save(userId, request));
    }

    // 자주 가는 장소 목록 조회
    @GetMapping
    public ResponseEntity<List<FavoritePlaceResponse>> findAll() {
            // 테스트용 유저 - 추후 수정 예정
            Long userId = 1L;
        return ResponseEntity.ok(favoritePlaceService.findAll(userId));
    }

    // 자주 가는 장소 수정
    @PatchMapping("/{placeId}")
    public ResponseEntity<Void> update(
            @PathVariable Long placeId,
            @RequestBody @Valid FavoritePlaceRequest.UpdateDto request) {
        favoritePlaceService.update(placeId, request);
        return ResponseEntity.ok().build();
    }

    // 자주 가는 장소 삭제
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> delete(@PathVariable Long placeId) {
        favoritePlaceService.delete(placeId);
        return ResponseEntity.ok().build();
    }
}

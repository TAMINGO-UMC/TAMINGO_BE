package app.tamingo.domain.favoriteplace.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceRequest;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceResponse;
import app.tamingo.domain.favoriteplace.entity.FavoritePlaceStandard;
import app.tamingo.domain.favoriteplace.exception.FavoritePlaceErrorCode;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceStandardRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FavoritePlaceService {
    private final FavoritePlaceStandardRepository favoritePlaceRepository;
    private final UserRepository userRepository;

    // 자주 가는 장소 등록
    @Transactional
    public Long save(Long userId, FavoritePlaceRequest.SaveDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 등록된 이름이나 주소인지 확인
        if (favoritePlaceRepository.existsByDuplicate(user, request.name(), request.address())) {
            throw new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_ALREADY_EXISTS);
        }

        FavoritePlaceStandard favoritePlaceStandard =  FavoritePlaceStandard.of(
                user,
                request.name(),
                request.address(),
                request.latitude(),
                request.longitude()
                );

        return favoritePlaceRepository.save(favoritePlaceStandard).getId();
    }

    // 특정 유저의 자주 가는 장소 목록 조회
    public List<FavoritePlaceResponse> findAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return favoritePlaceRepository.findAllByUser(user).stream()
                .map(FavoritePlaceResponse::from)
                .toList();
    }

    // 자주 가는 장소 정보 수정 (더티 체킹 사용)
    @Transactional
    public void update(Long id, FavoritePlaceRequest.UpdateDto request) {
        FavoritePlaceStandard favoritePlaceStandard = favoritePlaceRepository.findById(id)
                .orElseThrow(() -> new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_NOT_FOUND));

        // 이미 등록된 주소나 장소로 변경하는지 확인 (자신은 제외)
        if (favoritePlaceRepository.existsForUpdate(
                favoritePlaceStandard.getUser(),
                request.name(),
                request.address(),
                id)) {
            throw new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_ALREADY_EXISTS);        }

        // 엔티티 변경시 트랜잭션 종료와 함께 자동 db 반영
        favoritePlaceStandard.update(
                request.name(),
                request.address(),
                request.latitude(),
                request.longitude()
        );
    }

    // 자주 가는 장소 삭제
    @Transactional
    public void delete(Long id) {
        FavoritePlaceStandard favoritePlaceStandard = favoritePlaceRepository.findById(id)
                .orElseThrow(() -> new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_NOT_FOUND));

        favoritePlaceRepository.delete(favoritePlaceStandard);
    }
}

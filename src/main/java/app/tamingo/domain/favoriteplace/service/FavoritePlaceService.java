package app.tamingo.domain.favoriteplace.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceRequest;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceResponse;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceSimpleResponse;
import app.tamingo.domain.favoriteplace.entity.FavoritePlace;
import app.tamingo.domain.favoriteplace.exception.FavoritePlaceErrorCode;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.respository.FvpHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class FavoritePlaceService {
    private final FavoritePlaceRepository favoritePlaceRepository;
    private final UserRepository userRepository;
    private final FvpHistoryRepository fvpHistoryRepository;

    // 자주 가는 장소 등록
    @Transactional
    public Long save(Long userId, FavoritePlaceRequest.SaveDto request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 이미 등록된 이름이나 주소인지 확인
        if (favoritePlaceRepository.existsByDuplicate(user, request.name(), request.address())) {
            throw new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_ALREADY_EXISTS);
        }

        FavoritePlace favoritePlace =  FavoritePlace.of(
                user,
                request.name(),
                request.address(),
                request.latitude(),
                request.longitude(),
                request.isAiSuggested()
                );

        return favoritePlaceRepository.save(favoritePlace).getId();
    }

    // 특정 유저의 자주 가는 장소 목록 조회
    @Transactional
    public List<FavoritePlaceResponse> findAll(Long userId) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 이번주 월요일 계산
        LocalDateTime monday = LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                .atStartOfDay();

        return favoritePlaceRepository.findAllByUser(user).stream()
                .map(place -> {
                    // 실시간으로 이력 테이블에서 카운트 조회
                    int count = fvpHistoryRepository.countWeeklyVisits(user, place.getName(), monday);

                    return FavoritePlaceResponse.from(place, count);
                })
                .toList();
    }

    // 자주 가는 장소 정보 수정 (더티 체킹 사용)
    @Transactional
    public void update(Long id, FavoritePlaceRequest.UpdateDto request) {
        FavoritePlace favoritePlace = favoritePlaceRepository.findById(id)
                .orElseThrow(() -> new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_NOT_FOUND));

        // 이미 등록된 주소나 장소로 변경하는지 확인 (자신은 제외)
        if (favoritePlaceRepository.existsForUpdate(
                favoritePlace.getUser(),
                request.name(),
                request.address(),
                id)) {
            throw new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_ALREADY_EXISTS);        }

        // 엔티티 변경시 트랜잭션 종료와 함께 자동 db 반영
        favoritePlace.update(
                request.name(),
                request.address(),
                request.latitude(),
                request.longitude()
        );
    }

    // 자주 가는 장소 삭제
    @Transactional
    public void delete(Long id) {
        FavoritePlace favoritePlace = favoritePlaceRepository.findById(id)
                .orElseThrow(() -> new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_NOT_FOUND));

        favoritePlaceRepository.delete(favoritePlace);
    }

    // 온보딩 초기 세팅: 자주 가는 장소 등록
    @Transactional
    public void replaceAll(Long userId, List<FavoritePlaceRequest.SaveDto> list, int maxLimit) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // null이면 그냥 아무 것도 안 함
        if (list == null || list.isEmpty()) {
            favoritePlaceRepository.deleteAllByUser(user);
            return;
        }

        // 5개 제한
        if (list.size() > maxLimit) {
            throw new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_LIMIT_EXCEEDED);
        }

        // 요청 내부 중복 방지 (name, address)
        Set<String> names = new HashSet<>();
        Set<String> addresses = new HashSet<>();

        for (FavoritePlaceRequest.SaveDto p : list) {
            if (!names.add(p.name()) || !addresses.add(p.address())) {
                throw new CustomException(FavoritePlaceErrorCode.FAVORITE_PLACE_ALREADY_EXISTS);
            }
        }

        favoritePlaceRepository.deleteAllByUser(user);

        for (FavoritePlaceRequest.SaveDto p : list) {
            favoritePlaceRepository.save(
                    FavoritePlace.of(
                            user,
                            p.name(),
                            p.address(),
                            p.latitude(),
                            p.longitude(),
                            p.isAiSuggested()
                    )
            );
        }
    }

    // 일정/할 일에서 선택용 목록 조회
    public List<FavoritePlaceSimpleResponse> findAllSimple(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        return favoritePlaceRepository.findAllByUser(user).stream()
                .map(FavoritePlaceSimpleResponse::from)
                .toList();
    }

}
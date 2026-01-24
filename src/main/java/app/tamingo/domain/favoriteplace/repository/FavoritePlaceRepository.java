package app.tamingo.domain.favoriteplace.repository;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    // 유저가 등록한 모든 자주 가는 장소 목록 조회
    List<FavoritePlace> findAllByUser(User user);

}

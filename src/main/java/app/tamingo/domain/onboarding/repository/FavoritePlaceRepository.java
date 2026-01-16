package app.tamingo.domain.onboarding.repository;

import app.tamingo.domain.onboarding.entity.FavoritePlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {

    List<FavoritePlace> findAllByUserIdOrderByIdAsc(Long userId);

    long countByUserId(Long userId);

    void deleteAllByUserId(Long userId);
}
package app.tamingo.domain.favoriteplace.repository;

import app.tamingo.domain.favoriteplace.entity.FavoritePlaceStandard;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoritePlaceStandardRepository extends JpaRepository<FavoritePlaceStandard, Long> {

    // 유저가 등록한 모든 자주 가는 장소 목록 조회
    List<FavoritePlaceStandard> findAllByUser(User user);

    // 특정 유저가 이미 등록된 이름이나 주소로 또 등록하려고 시도하는 경우
    @Query("SELECT COUNT(f) > 0 FROM FavoritePlaceStandard f WHERE f.user = :user AND (f.name = :name OR f.address = :address)")
    boolean existsByDuplicate(@Param("user") User user, @Param("name") String name, @Param("address") String address);

    // 특정 유저가 이미 등록된 이름이나 주소로 수정하려고 시도시, 현재의 것을 제외한 중복 검사 (수정 안 하고 유지시 오류 터지면 안됨)
    @Query("SELECT COUNT(f) > 0 FROM FavoritePlaceStandard f " +
            "WHERE f.user = :user " +
            "AND (f.name = :name OR f.address = :address) " +
            "AND f.id <> :id")
    boolean existsForUpdate(@Param("user") User user,
                            @Param("name") String name,
                            @Param("address") String address,
                            @Param("id") Long id);

    // 특정 유저가 장소명'name'으로 저장한 장소가 있는지 확인
    boolean existsByUserAndName(User user, String name);

    // 자주 가는 장소 삭제
    void deleteAllByUser(User user);

    // 자주 가는 장소 개수 확인
    long countByUser(User user);
}

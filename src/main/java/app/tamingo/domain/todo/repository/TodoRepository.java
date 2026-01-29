package app.tamingo.domain.todo.repository;

import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 자주 가는 장소 추천 로직
    // 해당 유저가 이 장소명으로 등록한 일정이 몇 개인지 카운트(사용자가 확정한 할 일만)
    int countByUserAndPlaceNameAndIsLocationConfirmedTrue(User user, String placeName);

    // 완료되지 않은 할 일 중에서 지정 장소의 2km 내에 있는 할 일 조회
    @Query(value = """
        SELECT * FROM todo t 
        WHERE t.user_id = :userId 
        AND t.is_checked = false 
        AND t.schedule_id IS NULL
        AND t.latitude BETWEEN :minLat AND :maxLat 
        AND t.longitude BETWEEN :minLon AND :maxLon
        AND (6371 * acos(cos(radians(:latitude)) * cos(radians(t.latitude)) 
        * cos(radians(t.longitude) - radians(:longitude)) 
        + sin(radians(:latitude)) * sin(radians(t.latitude)))) <= 2.0
        """, nativeQuery = true)
    List<Todo> findNearbyTodos(
            @Param("userId") Long userId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon
    );

    // 후보군 조회 (이번 주 OR 날짜 미지정)
    // 완료되지 않은 할 일 중에서 이번 주 할 일 혹은 날짜 미지정
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
            "AND t.isChecked = false " +
            "AND t.schedule IS NULL " +
            "AND (t.targetDate BETWEEN :startDate AND :endDate OR t.targetDate IS NULL) " +
            "ORDER BY CASE WHEN t.targetDate IS NULL THEN 1 ELSE 0 END,t.targetDate ASC")
    List<Todo> findCandidateTodos(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 할일 카테고리가 사용 중이면 삭제 불가
    boolean existsByUserAndTodoCategory(User user, TodoCategory todoCategory);
}

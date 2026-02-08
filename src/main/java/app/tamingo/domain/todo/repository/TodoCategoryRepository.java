package app.tamingo.domain.todo.repository;

import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TodoCategoryRepository extends JpaRepository<TodoCategory, Long> {

    // 유저 카테고리 조회
    List<TodoCategory> findAllByUser(User user);

    // 카테고리 단건 조회 (유저 검증용)
    Optional<TodoCategory> findByIdAndUser(Long id, User user);

    // 이름 중복 체크
    boolean existsByUserAndName(User user, String name);

    @Query("select count(c) from TodoCategory c where c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

}

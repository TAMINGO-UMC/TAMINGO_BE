package app.tamingo.domain.todo.repository;

import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoAiLog;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TodoAiLogRepository extends JpaRepository<TodoAiLog, Long> {
    // 할 일에 매핑된 AI 로그 조회
    Optional<TodoAiLog> findByTodo(Todo todo);

    void deleteByUser(User user);

    long countByUser(User user);

    @Query("SELECT COALESCE(SUM(t.score), 0) FROM TodoAiLog t WHERE t.user = :user")
    int sumScoreByUser(@Param("user") User user);
}

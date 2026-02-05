package app.tamingo.domain.todo.repository;

import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoAiLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoAiLogRepository extends JpaRepository<TodoAiLog, Long> {
    // 할 일에 매핑된 AI 로그 조회
    Optional<TodoAiLog> findByTodo(Todo todo);
}

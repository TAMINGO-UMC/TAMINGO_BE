package app.tamingo.domain.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import app.tamingo.domain.todo.entity.TodoAiLog;

public interface TodoAiLogRepository extends JpaRepository<TodoAiLog, Long> {
}

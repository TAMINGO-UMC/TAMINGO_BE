package app.tamingo.domain.todo.repository;

import app.tamingo.domain.todo.entity.TodoAiLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoAiLogRepository extends JpaRepository<TodoAiLog, Long> {
}

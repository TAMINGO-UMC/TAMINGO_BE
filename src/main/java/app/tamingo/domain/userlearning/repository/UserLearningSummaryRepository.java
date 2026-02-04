package app.tamingo.domain.userlearning.repository;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.UserLearningSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLearningSummaryRepository extends JpaRepository<UserLearningSummary, Long> {
    Optional<UserLearningSummary> findByUser(User user);
}

package app.tamingo.domain.userlearning.repository;

import app.tamingo.domain.home.entity.enums.TimeSlot;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.UserLearningPattern;
import app.tamingo.domain.userlearning.entity.enums.RouteType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLearningPatternRepository extends JpaRepository<UserLearningPattern, Long> {
    Optional<UserLearningPattern> findByUserAndTimeSlotAndRouteType(User user, TimeSlot timeSlot, RouteType routeType);
}

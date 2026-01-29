package app.tamingo.domain.useractivetime.repository;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.useractivetime.entity.UserActiveTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserActiveTimeRepository extends JpaRepository<UserActiveTime, Long> {

    Optional<UserActiveTime> findByUserId(Long userId);

}

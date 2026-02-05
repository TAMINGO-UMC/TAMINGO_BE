package app.tamingo.domain.notification.repository;

import app.tamingo.domain.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findAllByUserIdAndIsActiveTrue(Long userId);
    Optional<DeviceToken> findByToken(String token);
}

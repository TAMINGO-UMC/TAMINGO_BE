package app.tamingo.domain.user.repository;

import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 유저 조회
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
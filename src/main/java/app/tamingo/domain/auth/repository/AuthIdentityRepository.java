package app.tamingo.domain.auth.repository;

import app.tamingo.domain.auth.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {

    boolean existsByProviderAndEmail(AuthProvider provider, String email);

    // LOCAL일 때만 이메일 중복 체크
    default boolean existsLocalByEmail(String email) {
        return existsByProviderAndEmail(AuthProvider.LOCAL, email);
    }
}
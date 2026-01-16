package app.tamingo.domain.auth.repository;

import app.tamingo.domain.auth.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {
    Optional<AuthIdentity> findByProviderAndEmail(AuthProvider provider, String email);
    boolean existsByProviderAndEmail(AuthProvider provider, String email);
}
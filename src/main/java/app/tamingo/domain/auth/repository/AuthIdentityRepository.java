package app.tamingo.domain.auth.repository;

import app.tamingo.domain.auth.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, Long> {

    @Modifying
    @Query("delete from AuthIdentity ai where ai.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    Optional<AuthIdentity> findByProviderAndEmail(AuthProvider provider, String email);
    boolean existsByProviderAndEmail(AuthProvider provider, String email);

    Optional<AuthIdentity> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
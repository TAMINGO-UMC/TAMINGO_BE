package app.tamingo.domain.auth.entity;

import app.tamingo.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_identities",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "email"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthIdentity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(length = 255)
    private String email;

    @Column(name="password_hash", length = 255)
    private String passwordHash;

    @Column(name="provider_user_id", length = 255)
    private String providerUserId;

    public static AuthIdentity createLocal(Long userId, String email, String passwordHash) {
        AuthIdentity ai = new AuthIdentity();
        ai.userId = userId;
        ai.provider = AuthProvider.LOCAL;
        ai.email = email;
        ai.passwordHash = passwordHash;
        return ai;
    }
}
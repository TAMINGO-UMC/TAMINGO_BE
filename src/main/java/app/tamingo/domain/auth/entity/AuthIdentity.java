package app.tamingo.domain.auth.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "auth_identities",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "email"}),
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthIdentity extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(length = 255)
    private String email;

    @Column(name="password_hash", length = 255)
    private String passwordHash;

    @Column(name="provider_user_id", length = 255)
    private String providerUserId;

    public static AuthIdentity createLocal(User user, String email, String passwordHash) {
        AuthIdentity ai = new AuthIdentity();
        ai.user = user;
        ai.provider = AuthProvider.LOCAL;
        ai.email = email;
        ai.passwordHash = passwordHash;
        return ai;
    }
}
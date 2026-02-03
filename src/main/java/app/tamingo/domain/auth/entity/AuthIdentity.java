package app.tamingo.domain.auth.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "auth_identities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_auth_identities_provider_provider_user_id",
                        columnNames = {"provider", "provider_user_id"}
                ),
                @UniqueConstraint(
                        name = "uq_auth_identities_provider_email",
                        columnNames = {"provider", "email"}
                )
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

    @Builder(builderMethodName = "internalBuilder")
    private AuthIdentity(
            User user,
            AuthProvider provider,
            String email,
            String passwordHash,
            String providerUserId
    ) {
        this.user = user;
        this.provider = provider;
        this.email = email;
        this.passwordHash = passwordHash;
        this.providerUserId = providerUserId;
    }

    public static AuthIdentity createLocal(User user, String email, String passwordHash) {
        return AuthIdentity.internalBuilder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .email(email)
                .passwordHash(passwordHash)
                .providerUserId(null)
                .build();
    }

    public static AuthIdentity createKakao(User user, String providerUserId, String email) {
        return AuthIdentity.internalBuilder()
                .user(user)
                .provider(AuthProvider.KAKAO)
                .providerUserId(providerUserId)
                .email(email)
                .passwordHash(null)
                .build();
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
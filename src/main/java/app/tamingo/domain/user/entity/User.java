package app.tamingo.domain.user.entity;

import app.tamingo.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_users_email", columnNames = "email")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private boolean onboardingCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Builder(builderMethodName = "internalBuilder")
    private User(
            String email,
            String nickname,
            boolean onboardingCompleted,
            UserStatus status
    ) {
        this.email = email;
        this.nickname = nickname;
        this.onboardingCompleted = onboardingCompleted;
        this.status = (status != null) ? status : UserStatus.ACTIVE;
    }

    // 생성 메서드
    public static User of(String email, String nickname) {
        return User.internalBuilder()
                .email(email)
                .nickname(nickname)
                .onboardingCompleted(false)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    public void withdraw() {
        this.status = UserStatus.DELETED;
    }
}
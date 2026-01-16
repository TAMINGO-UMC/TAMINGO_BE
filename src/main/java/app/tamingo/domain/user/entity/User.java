package app.tamingo.domain.user.entity;

import app.tamingo.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false)
    private boolean onboardingCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    // 생성 메서드
    public static User create(String email, String nickname) {
        User user = new User();
        user.email = email;
        user.nickname = nickname;
        return user;
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }
}
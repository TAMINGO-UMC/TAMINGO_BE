package app.tamingo.domain.user.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_terms_agreements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_terms",
                        columnNames = {"user_id", "terms_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTermsAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    @Column(nullable = false)
    private boolean isAgreed;

    private LocalDateTime agreedAt;

    @Builder(builderMethodName = "internalBuilder")
    private UserTermsAgreement(
            User user,
            Terms terms,
            boolean isAgreed,
            LocalDateTime agreedAt
    ) {
        this.user = user;
        this.terms = terms;
        this.isAgreed = isAgreed;
        this.agreedAt = agreedAt;
    }

    // 생성 메서드
    public static UserTermsAgreement agree(User user, Terms terms, boolean isAgreed) {
        return UserTermsAgreement.internalBuilder()
                .user(user)
                .terms(terms)
                .isAgreed(isAgreed)
                .agreedAt(isAgreed ? LocalDateTime.now() : null)
                .build();
    }
}
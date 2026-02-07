package app.tamingo.domain.userlearning.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "personalization_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 오차 로그 수집 설정 기본 on
    @Column(name = "error_log_enabled", nullable = false)
    private boolean errorLogEnabled = true;

    @Builder(builderMethodName = "internalBuilder")
    private PersonalSetting(User user, boolean errorLogEnabled) {
        this.user = user;
        this.errorLogEnabled = errorLogEnabled;
    }

    public static PersonalSetting of(User user, boolean errorLogEnabled) {
        return PersonalSetting.internalBuilder()
                .user(user)
                .errorLogEnabled(errorLogEnabled)
                .build();
    }

    // 오차 로그 수집 설정 수정
    public void update() {
        if (this.errorLogEnabled) {
            this.errorLogEnabled = false;
        }
    }

}

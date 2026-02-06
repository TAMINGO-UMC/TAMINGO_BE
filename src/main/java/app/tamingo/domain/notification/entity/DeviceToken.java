package app.tamingo.domain.notification.entity;

import app.tamingo.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "device_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String token;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Builder(access = AccessLevel.PRIVATE)
    public DeviceToken(Long userId, String token, boolean isActive) {
        this.userId = userId;
        this.token = token;
        this.isActive = isActive;
    }

    public static DeviceToken of(Long userId, String token) {
        return DeviceToken.builder()
                .userId(userId)
                .token(token)
                .isActive(true)
                .build();
    }

    public void deactivate() {
        this.isActive = false;
    }
}

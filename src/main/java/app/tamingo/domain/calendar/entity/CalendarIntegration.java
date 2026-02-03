package app.tamingo.domain.calendar.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.calendar.enums.CalendarIntegrationStatus;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "calendar_integration",
        indexes = {
                @Index(name = "idx_calendar_integration_user_id", columnList = "user_id")
        }
)
public class CalendarIntegration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 연동 제공자 (APPLE)
    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    // Apple -> App 동기화 여부
    @Column(name = "sync_from_apple", nullable = false)
    private boolean syncFromApple;

    // App -> Apple 동기화 여부
    @Column(name = "sync_to_apple", nullable = false)
    private boolean syncToApple;

    // 연동 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CalendarIntegrationStatus status;

    // 마지막 동기화 성공 시간
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Builder(builderMethodName = "internalBuilder")
    private CalendarIntegration(
            User user,
            String provider,
            boolean syncFromApple,
            boolean syncToApple,
            CalendarIntegrationStatus status,
            LocalDateTime lastSyncedAt
    ) {
        this.user = user;
        this.provider = provider;
        this.syncFromApple = syncFromApple;
        this.syncToApple = syncToApple;
        this.status = status;
        this.lastSyncedAt = lastSyncedAt;
    }

    public static CalendarIntegration of(
            User user,
            String provider,
            boolean syncFromApple,
            boolean syncToApple,
            CalendarIntegrationStatus status,
            LocalDateTime lastSyncedAt
    ) {
        return CalendarIntegration.internalBuilder()
                .user(user)
                .provider(provider)
                .syncFromApple(syncFromApple)
                .syncToApple(syncToApple)
                .status(status)
                .lastSyncedAt(lastSyncedAt)
                .build();
    }

    // 토글 변경
    public void updateSyncFlags(boolean syncFromApple, boolean syncToApple) {
        this.syncFromApple = syncFromApple;
        this.syncToApple = syncToApple;
    }

    // 상태 변경
    public void updateStatus(CalendarIntegrationStatus status) {
        this.status = status;
    }

    // 동기화 성공 처리
    public void markSyncedNow() {
        this.lastSyncedAt = LocalDateTime.now();
        this.status = CalendarIntegrationStatus.ACTIVE;
    }

    // 동기화 시작 처리
    public void markSyncing() {
        this.status = CalendarIntegrationStatus.SYNCING;
    }

    // 오류 처리
    public void markError() {
        this.status = CalendarIntegrationStatus.ERROR;
    }
}

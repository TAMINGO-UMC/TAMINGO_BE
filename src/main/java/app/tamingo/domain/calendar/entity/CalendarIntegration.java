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
        uniqueConstraints = {
                // 유저당 애플 연동은 1개만 존재
                @UniqueConstraint(name = "uk_calendar_integration_user", columnNames = {"user_id"})
        },
        indexes = {
                @Index(name = "idx_calendar_integration_user_id", columnList = "user_id")
        }
)
public class CalendarIntegration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 연동 소유자(유저)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Apple -> App 동기화 ON/OFF
    @Column(name = "sync_from_apple", nullable = false)
    private boolean syncFromApple;

    // 연동 상태 (ACTIVE/SYNCING/ERROR 등)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CalendarIntegrationStatus status;

    // 마지막 동기화 성공 시간
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Builder(builderMethodName = "internalBuilder")
    private CalendarIntegration(
            User user,
            boolean syncFromApple,
            CalendarIntegrationStatus status,
            LocalDateTime lastSyncedAt
    ) {
        this.user = user;
        this.syncFromApple = syncFromApple;
        this.status = status;
        this.lastSyncedAt = lastSyncedAt;
    }

    // 최초 연동 생성 (기본: 동기화 ON)
    public static CalendarIntegration of(User user) {
        return CalendarIntegration.internalBuilder()
                .user(user)
                .syncFromApple(true)
                .status(CalendarIntegrationStatus.ACTIVE)
                .lastSyncedAt(null)
                .build();
    }

    // 동기화 스위치 변경
    public void updateSyncFromApple(boolean syncFromApple) {
        this.syncFromApple = syncFromApple;
    }

    // 동기화 진행중 표시(선택)
    public void markSyncing() {
        this.status = CalendarIntegrationStatus.SYNCING;
    }

    // 동기화 성공 처리
    public void markSyncedNow() {
        this.status = CalendarIntegrationStatus.ACTIVE;
        this.lastSyncedAt = LocalDateTime.now();
    }

    // 동기화 실패 처리
    public void markError() {
        this.status = CalendarIntegrationStatus.ERROR;
    }
}

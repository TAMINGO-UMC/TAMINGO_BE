package app.tamingo.domain.calendar.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.calendar.enums.ConflictStatus;
import app.tamingo.domain.calendar.enums.LinkStatus;
import app.tamingo.domain.calendar.enums.SyncDirection;
import app.tamingo.domain.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "external_task_mapping",
        indexes = {
                @Index(name = "idx_ext_mapping_integration_id", columnList = "integration_id"),
                @Index(name = "idx_ext_mapping_schedule_id", columnList = "schedule_id"),
                @Index(name = "idx_ext_mapping_calendar_event_id", columnList = "calendar_event_id")
        }
)
public class ExternalTaskMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 애플캘린더 연동 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "integration_id", nullable = false)
    private CalendarIntegration integration;

    // 일정 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // 캘린더 이벤트 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_event_id", nullable = false)
    private CalendarEvent calendarEvent;

    // 연결 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "link_status", nullable = false, length = 30)
    private LinkStatus linkStatus;

    // 동기화 방향
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_direction", nullable = false, length = 30)
    private SyncDirection syncDirection;

    // 충돌 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_status", length = 30)
    private ConflictStatus conflictStatus;

    // 마지막 동기화 시각
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Builder(builderMethodName = "internalBuilder")
    private ExternalTaskMapping(
            CalendarIntegration integration,
            Schedule schedule,
            CalendarEvent calendarEvent,
            LinkStatus linkStatus,
            SyncDirection syncDirection,
            ConflictStatus conflictStatus,
            LocalDateTime lastSyncedAt
    ) {
        this.integration = integration;
        this.schedule = schedule;
        this.calendarEvent = calendarEvent;
        this.linkStatus = linkStatus;
        this.syncDirection = syncDirection;
        this.conflictStatus = conflictStatus;
        this.lastSyncedAt = lastSyncedAt;
    }

    public static ExternalTaskMapping of(
            CalendarIntegration integration,
            Schedule schedule,
            CalendarEvent calendarEvent,
            LinkStatus linkStatus,
            SyncDirection syncDirection,
            ConflictStatus conflictStatus,
            LocalDateTime lastSyncedAt
    ) {
        return ExternalTaskMapping.internalBuilder()
                .integration(integration)
                .schedule(schedule)
                .calendarEvent(calendarEvent)
                .linkStatus(linkStatus)
                .syncDirection(syncDirection)
                .conflictStatus(conflictStatus)
                .lastSyncedAt(lastSyncedAt)
                .build();
    }

    // 동기화 성공 처리
    public void markSyncedNow() {
        this.lastSyncedAt = LocalDateTime.now();
        this.conflictStatus = ConflictStatus.NONE;
    }

    // 충돌 처리
    public void markConflict() {
        this.conflictStatus = ConflictStatus.CONFLICT;
    }

    // 연결 해제 처리
    public void unlink() {
        this.linkStatus = LinkStatus.UNLINKED;
    }
}

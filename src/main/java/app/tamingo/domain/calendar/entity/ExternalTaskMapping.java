package app.tamingo.domain.calendar.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.calendar.enums.LinkStatus;
import app.tamingo.domain.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "external_task_mapping",
        uniqueConstraints = {
                // 한 Apple 이벤트는 한 Schedule에만 매핑
                @UniqueConstraint(
                        name = "uk_ext_mapping_integration_event",
                        columnNames = {"integration_id", "calendar_event_id"}
                ),
                // 한 Schedule은 한 외부 매핑만 가짐
                @UniqueConstraint(
                        name = "uk_ext_mapping_schedule",
                        columnNames = {"schedule_id"}
                )
        },
        indexes = {
                @Index(name = "idx_ext_mapping_integration_id", columnList = "integration_id"),
                @Index(name = "idx_ext_mapping_calendar_event_id", columnList = "calendar_event_id"),
                @Index(name = "idx_ext_mapping_schedule_id", columnList = "schedule_id")
        }
)
public class ExternalTaskMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 연동(유저의 애플 연동)인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "integration_id", nullable = false)
    private CalendarIntegration integration;

    // 앱 내부 일정
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // Apple 이벤트 스냅샷
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "calendar_event_id", nullable = false)
    private CalendarEvent calendarEvent;

    // LINKED: Apple 값으로 덮어씀 / UNLINKED: 앱 수정됨(동기화 스킵)
    @Enumerated(EnumType.STRING)
    @Column(name = "link_status", nullable = false, length = 30)
    private LinkStatus linkStatus;

    // 마지막 동기화 처리 시간(선택)
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Builder(builderMethodName = "internalBuilder")
    private ExternalTaskMapping(
            CalendarIntegration integration,
            Schedule schedule,
            CalendarEvent calendarEvent,
            LinkStatus linkStatus,
            LocalDateTime lastSyncedAt
    ) {
        this.integration = integration;
        this.schedule = schedule;
        this.calendarEvent = calendarEvent;
        this.linkStatus = linkStatus;
        this.lastSyncedAt = lastSyncedAt;
    }

    // 최초 매핑 생성은 LINKED가 기본
    public static ExternalTaskMapping linked(CalendarIntegration integration, Schedule schedule, CalendarEvent calendarEvent) {
        return ExternalTaskMapping.internalBuilder()
                .integration(integration)
                .schedule(schedule)
                .calendarEvent(calendarEvent)
                .linkStatus(LinkStatus.LINKED)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    // LINKED 여부
    public boolean isLinked() {
        return this.linkStatus == LinkStatus.LINKED;
    }

    // UNLINKED 여부
    public boolean isUnlinked() {
        return this.linkStatus == LinkStatus.UNLINKED;
    }

    // B안 핵심: 앱에서 수정/삭제하려는 순간 호출 (매핑은 삭제하지 않음 => 중복 생성 방지)
    public void unlink() {
        this.linkStatus = LinkStatus.UNLINKED;
    }

    // 동기화 처리 시간 기록(선택)
    public void markSyncedNow() {
        this.lastSyncedAt = LocalDateTime.now();
    }

    // CalendarEvent 참조 교체가 필요할 때만 사용(선택)
    public void rebindEvent(CalendarEvent calendarEvent) {
        this.calendarEvent = calendarEvent;
    }
}

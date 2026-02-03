package app.tamingo.domain.calendar.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "calendar_event",
        indexes = {
                @Index(name = "idx_calendar_event_user_id", columnList = "user_id"),
                @Index(name = "idx_calendar_event_integration_id", columnList = "integration_id"),
                @Index(name = "idx_calendar_event_external_uid", columnList = "external_event_uid")
        }
)
public class CalendarEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 애플캘린더 연동 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "integration_id", nullable = false)
    private CalendarIntegration integration;

    // 유저 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 캘린더 제공자 (APPLE)
    @Column(name = "provider", nullable = false, length = 30)
    private String provider;

    // 외부 이벤트 UID
    @Column(name = "external_event_uid", nullable = false, length = 255)
    private String externalEventUid;

    // 외부 캘린더 ID
    @Column(name = "calendar_external_id", length = 255)
    private String calendarExternalId;

    // 캘린더 이름
    @Column(name = "calendar_name", length = 255)
    private String calendarName;

    // 일정 제목
    @Column(name = "title", length = 255)
    private String title;

    // 시작 시각
    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    // 종료 시각
    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    // 종일 일정 여부
    @Column(name = "is_all_day", nullable = false)
    private boolean isAllDay;

    // 타임존
    @Column(name = "timezone", length = 50)
    private String timezone;

    // 장소
    @Column(name = "location", length = 255)
    private String location;

    // 메모
    @Lob
    @Column(name = "notes")
    private String notes;

    // 외부 마지막 수정 시각
    @Column(name = "last_external_modified_at")
    private LocalDateTime lastExternalModifiedAt;

    // 삭제 시각 (소프트 딜리트)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete() {
        if (this.deletedAt != null) return;
        this.deletedAt = LocalDateTime.now();
    }

    @Builder(builderMethodName = "internalBuilder")
    private CalendarEvent(
            CalendarIntegration integration,
            User user,
            String provider,
            String externalEventUid,
            String calendarExternalId,
            String calendarName,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            boolean isAllDay,
            String timezone,
            String location,
            String notes,
            LocalDateTime lastExternalModifiedAt
    ) {
        this.integration = integration;
        this.user = user;
        this.provider = provider;
        this.externalEventUid = externalEventUid;
        this.calendarExternalId = calendarExternalId;
        this.calendarName = calendarName;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isAllDay = isAllDay;
        this.timezone = timezone;
        this.location = location;
        this.notes = notes;
        this.lastExternalModifiedAt = lastExternalModifiedAt;
    }

    public static CalendarEvent of(
            CalendarIntegration integration,
            User user,
            String provider,
            String externalEventUid,
            String calendarExternalId,
            String calendarName,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            boolean isAllDay,
            String timezone,
            String location,
            String notes,
            LocalDateTime lastExternalModifiedAt
    ) {
        return CalendarEvent.internalBuilder()
                .integration(integration)
                .user(user)
                .provider(provider)
                .externalEventUid(externalEventUid)
                .calendarExternalId(calendarExternalId)
                .calendarName(calendarName)
                .title(title)
                .startAt(startAt)
                .endAt(endAt)
                .isAllDay(isAllDay)
                .timezone(timezone)
                .location(location)
                .notes(notes)
                .lastExternalModifiedAt(lastExternalModifiedAt)
                .build();
    }
}

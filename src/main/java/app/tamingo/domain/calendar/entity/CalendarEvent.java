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
        uniqueConstraints = {
                // 중복 저장 방지
                @UniqueConstraint(
                        name = "uk_calendar_event_integration_external_uid",
                        columnNames = {"integration_id", "external_event_uid"}
                )
        },
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

    // 어떤 애플 연동에서 들어온 이벤트인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "integration_id", nullable = false)
    private CalendarIntegration integration;

    // 유저 FK(조회 최적화용)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 외부 이벤트 UID(EventKit의 calendarItemExternalIdentifier 추천)
    @Column(name = "external_event_uid", nullable = false, length = 255)
    private String externalEventUid;

    // 외부 캘린더 식별자
    @Column(name = "calendar_external_id", length = 255)
    private String calendarExternalId;

    // 캘린더 이름
    @Column(name = "calendar_name", length = 255)
    private String calendarName;

    // 제목
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

    // 타임존 ex) Asia/Seoul
    @Column(name = "timezone", length = 50)
    private String timezone;

    // 장소
    @Column(name = "location", length = 255)
    private String location;

    // 메모
    @Lob
    @Column(name = "notes")
    private String notes;

    // Apple 측 마지막 수정 시간(선택)
    @Column(name = "last_external_modified_at")
    private LocalDateTime lastExternalModifiedAt;

    // Apple에서 삭제된 이벤트 처리(soft delete)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder(builderMethodName = "internalBuilder")
    private CalendarEvent(
            CalendarIntegration integration,
            User user,
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
            LocalDateTime lastExternalModifiedAt,
            LocalDateTime deletedAt
    ) {
        this.integration = integration;
        this.user = user;
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
        this.deletedAt = deletedAt;
    }

    // 최초 생성
    public static CalendarEvent of(
            CalendarIntegration integration,
            User user,
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
                .deletedAt(null)
                .build();
    }

    // 삭제 여부
    public boolean isDeleted() {
        return deletedAt != null;
    }

    // Apple에서 삭제되었다고 판단되면 호출
    public void markDeletedNow() {
        if (this.deletedAt == null) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    // Apple에서 받은 최신 값으로 갱신(upsert 시 사용)
    public void updateFromApple(
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
        // Apple에서 다시 살아난 케이스 대응
        this.deletedAt = null;
    }
}

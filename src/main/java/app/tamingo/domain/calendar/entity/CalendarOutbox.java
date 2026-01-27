package app.tamingo.domain.calendar.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.calendar.enums.OutboxOpType;
import app.tamingo.domain.calendar.enums.OutboxStatus;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "calendar_outbox",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_calendar_outbox_integration_idempotency",
                        columnNames = {"integration_id", "idempotency_key"}
                )
        },
        indexes = {
                @Index(name = "idx_calendar_outbox_user_id", columnList = "user_id"),
                @Index(name = "idx_calendar_outbox_integration_id", columnList = "integration_id"),
                @Index(name = "idx_calendar_outbox_status_createdAt", columnList = "status, createdAt"),
                @Index(name = "idx_calendar_outbox_integration_status", columnList = "integration_id, status")
        }
)
public class CalendarOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 애플캘린더 연동 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "integration_id", nullable = false)
    private CalendarIntegration integration;

    // 캘린더 이벤트 외래키 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_event_id")
    private CalendarEvent calendarEvent;

    // 대상 외부 이벤트 UID (nullable)
    @Column(name = "target_external_event_uid", length = 255)
    private String targetExternalEventUid;

    // 작업유형
    @Enumerated(EnumType.STRING)
    @Column(name = "op_type", nullable = false, length = 30)
    private OutboxOpType opType;

    // 멱등성 키
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    // 작업 데이터 (json)
    @Lob
    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    private String payloadJson;

    // 작업 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OutboxStatus status;

    // 시도 횟수
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    // 마지막 시도 시각
    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    // 처리 완료 시각
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // 에러 메시지
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Builder(builderMethodName = "internalBuilder")
    private CalendarOutbox(
            User user,
            CalendarIntegration integration,
            CalendarEvent calendarEvent,
            String targetExternalEventUid,
            OutboxOpType opType,
            String idempotencyKey,
            String payloadJson,
            OutboxStatus status,
            int attemptCount,
            LocalDateTime lastAttemptAt,
            LocalDateTime processedAt,
            String errorMessage
    ) {
        this.user = user;
        this.integration = integration;
        this.calendarEvent = calendarEvent;
        this.targetExternalEventUid = targetExternalEventUid;
        this.opType = opType;
        this.idempotencyKey = idempotencyKey;
        this.payloadJson = payloadJson;
        this.status = status;
        this.attemptCount = attemptCount;
        this.lastAttemptAt = lastAttemptAt;
        this.processedAt = processedAt;
        this.errorMessage = errorMessage;
    }

    public static CalendarOutbox of(
            User user,
            CalendarIntegration integration,
            CalendarEvent calendarEvent,
            String targetExternalEventUid,
            OutboxOpType opType,
            String idempotencyKey,
            String payloadJson
    ) {
        return CalendarOutbox.internalBuilder()
                .user(user)
                .integration(integration)
                .calendarEvent(calendarEvent)
                .targetExternalEventUid(targetExternalEventUid)
                .opType(opType)
                .idempotencyKey(idempotencyKey)
                .payloadJson(payloadJson)
                .status(OutboxStatus.PENDING)
                .attemptCount(0)
                .build();
    }

    // 처리 시작
    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    // 시도 기록
    public void recordAttempt() {
        this.attemptCount += 1;
        this.lastAttemptAt = LocalDateTime.now();
    }

    // 성공 처리
    public void markSuccess() {
        this.status = OutboxStatus.SUCCESS;
        this.processedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    // 실패 처리
    public void markFailed(String errorMessage) {
        this.status = OutboxStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}

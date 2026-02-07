package app.tamingo.domain.calendar.dto;

import app.tamingo.domain.calendar.enums.CalendarIntegrationStatus;

import java.time.LocalDateTime;

//연동 상태 응답
public record AppleCalendarIntegrationStatusResponse(
        boolean enabled,                   //enabled=true면 동기화 대상(ACTIVE)
        CalendarIntegrationStatus status,   //ACTIVE/INACTIVE/SYNCING/ERROR
        LocalDateTime lastSyncedAt          //마지막 동기화 성공 시간
) {
}

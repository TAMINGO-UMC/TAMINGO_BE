package app.tamingo.domain.calendar.enums;

public enum CalendarIntegrationStatus {
    ACTIVE,     // 정상 연동됨
    INACTIVE,   // 사용자가 연동 비활성화
    SYNCING,    // 동기화 중
    ERROR       // 동기화 오류
}
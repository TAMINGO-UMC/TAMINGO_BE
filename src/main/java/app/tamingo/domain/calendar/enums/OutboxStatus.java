package app.tamingo.domain.calendar.enums;

public enum OutboxStatus {
    PENDING,     // 대기
    PROCESSING,  // 처리 중
    SUCCESS,     // 처리 완료
    FAILED       // 실패(재시도 가능/불가 정책은 attemptCount로)
}

package app.tamingo.domain.schedule.enums;

/**
 * 일정 수행 결과 상태
 * - 주간 리포트 집계에서 분모/분자 정책에 직접 영향을 주므로 명확히 정의
 */
public enum ScheduleResultStatus {
    ON_TIME,   // 정시 도착
    LATE,      // 지각 도착
    NO_SHOW,   // 미도착
    CANCELED, // 취소
    PENDING // 결과 미확정(길찾기 시작만 한 상태 포함)
}
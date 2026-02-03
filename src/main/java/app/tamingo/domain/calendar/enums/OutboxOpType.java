package app.tamingo.domain.calendar.enums;

public enum OutboxOpType {
    CREATE,   // 외부(Apple) 이벤트 생성
    UPDATE,   // 외부 이벤트 수정
    DELETE    // 외부 이벤트 삭제
}
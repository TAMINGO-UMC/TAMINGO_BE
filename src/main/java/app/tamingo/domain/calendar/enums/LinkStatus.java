package app.tamingo.domain.calendar.enums;

public enum LinkStatus {
    LINKED,      // Apple 이벤트 <-> Schedule 정상 연결(동기화 시 덮어쓰기 대상)
    UNLINKED    // 앱에서 수정/삭제로 연동 끊김
}
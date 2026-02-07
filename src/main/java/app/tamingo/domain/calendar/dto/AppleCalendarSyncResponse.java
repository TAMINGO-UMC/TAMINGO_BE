package app.tamingo.domain.calendar.dto;

import java.time.LocalDateTime;

// 설명: 동기화 결과 요약 응답 DTO
public record AppleCalendarSyncResponse(
        int createdSchedules,   //새로 생성된 schedule 수
        int updatedSchedules,   //LINKED라서 덮어쓰기 업데이트한 schedule 수
        int skippedSchedules,   //UNLINKED라서 업데이트 스킵한 schedule 수
        int upsertedEvents,     //upsert 처리된 calendar_event 수
        LocalDateTime syncedAt  //서버 기준 동기화 완료 시각
) {}

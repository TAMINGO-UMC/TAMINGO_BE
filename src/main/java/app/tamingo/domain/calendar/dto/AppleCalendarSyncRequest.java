package app.tamingo.domain.calendar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 설명: iOS(EventKit)에서 읽은 최소 이벤트 리스트를 서버로 보내는 요청 DTO
public record AppleCalendarSyncRequest(
        @NotNull(message = "필수 값입니다")
        List<@Valid AppleCalendarEventItem> events
) {

    // 설명: 최소 이벤트 1건
    public record AppleCalendarEventItem(
            @NotBlank(message = "필수 값입니다")
            String externalEventUid,   // 설명: calendarItemExternalIdentifier

            String title,              // 설명: 일정 제목(없으면 서버에서 '일정'으로 보정)

            @NotBlank(message = "필수 값입니다")
            String startAt,            // 설명: startDate (ISO-8601 문자열, +09:00 포함 추천)

            @NotBlank(message = "필수 값입니다")
            String endAt,              // 설명: endDate (ISO-8601 문자열)

            String location,           // 설명: 장소

            @NotNull(message = "필수 값입니다")
            Boolean isAllDay           // 설명: 종일 여부 (EventKit isAllDay)
    ) {}
}

package app.tamingo.domain.calendar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 설명: iOS(EventKit)에서 읽은 이벤트 리스트를 서버로 보내는 요청 DTO
public record AppleCalendarSyncRequest(
        @NotEmpty List<@Valid AppleCalendarEventItem> events
) {
    // 설명: 이벤트 1건 DTO
    public record AppleCalendarEventItem(
            @NotNull String externalEventUid,     //EventKit 고유키(추천: calendarItemExternalIdentifier)
            String calendarExternalId,            //캘린더 식별자(선택)
            String calendarName,                  //캘린더 이름(선택)
            String title,                         //일정 제목(선택)
            @NotNull String startAt,              //시작(ISO-8601 문자열 권장, 서버에서 파싱)
            @NotNull String endAt,                //종료(ISO-8601 문자열 권장)
            boolean isAllDay,                     //종일 여부
            String timezone,                      //타임존ex) Asia/Seoul
            String location,                      //장소
            String notes,                         //메모
            String lastExternalModifiedAt,        //Apple 마지막 수정시간
            Boolean deleted                       // 삭제 이벤트면 true
    ) {}
}

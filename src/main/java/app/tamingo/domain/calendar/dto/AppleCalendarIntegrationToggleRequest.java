package app.tamingo.domain.calendar.dto;

import jakarta.validation.constraints.NotNull;

//연동 토글 요청(단방향 Apple -> Tamingo만)
public record AppleCalendarIntegrationToggleRequest(
        @NotNull Boolean enabled //true면 ACTIVE, false면 INACTIVE
) {
}

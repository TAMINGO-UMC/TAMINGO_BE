package app.tamingo.domain.notificationsetting.dto;

public class NotificationSettingRequest {

    public record UpdateDto(
            Boolean departureAlertEnabled,
            Integer departureLeadMinutes,
            Boolean realtimeTransitEnabled,
            Boolean todoProposalEnabled,
            Boolean locationMoveCheckEnabled,
            Boolean routineAlertEnabled
    ) {
        public UpdateDto {
            // 수정 시 값이 누락되면 기존 로직을 유지하거나 기본값으로 덮어씀
            departureAlertEnabled = (departureAlertEnabled == null) ? false : departureAlertEnabled;
            departureLeadMinutes = (departureLeadMinutes == null) ? 30 : departureLeadMinutes;
            realtimeTransitEnabled = (realtimeTransitEnabled == null) ? false : realtimeTransitEnabled;
            todoProposalEnabled = (todoProposalEnabled == null) ? false : todoProposalEnabled;
            locationMoveCheckEnabled = (locationMoveCheckEnabled == null) ? false : locationMoveCheckEnabled;
            routineAlertEnabled = (routineAlertEnabled == null) ? false : routineAlertEnabled;
        }
    }
}

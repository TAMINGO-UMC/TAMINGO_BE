package app.tamingo.domain.notificationsetting.dto;

import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import lombok.Builder;

@Builder
public record NotificationSettingResponse(
        boolean departureAlertEnabled,
        int departureLeadMinutes,
        boolean realtimeTransitEnabled,
        boolean todoProposalEnabled,
        boolean locationMoveCheckEnabled) {
    public static NotificationSettingResponse from(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .departureAlertEnabled(setting.isDepartureAlertEnabled())
                .departureLeadMinutes(setting.getDepartureLeadMinutes())
                .realtimeTransitEnabled(setting.isRealtimeTransitEnabled())
                .todoProposalEnabled(setting.isTodoProposalEnabled())
                .locationMoveCheckEnabled(setting.isLocationMoveCheckEnabled())
                .build();

    }
}
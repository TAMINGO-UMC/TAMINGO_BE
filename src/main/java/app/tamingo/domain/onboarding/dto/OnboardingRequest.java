package app.tamingo.domain.onboarding.dto;

import app.tamingo.domain.onboarding.entity.TransportType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OnboardingRequest(
        @Valid @NotNull ActiveTime activeTime,
        @Valid @NotNull @Size(max = 5) List<FavoritePlace> favoritePlaces,
        @Valid @NotNull List<TransportPref> transportPreferences,
        @Valid @NotNull NotificationSetting notificationSetting
) {
    public record ActiveTime(
            @NotNull String startTime,
            @NotNull String endTime,
            boolean monEnabled,
            boolean tueEnabled,
            boolean wedEnabled,
            boolean thuEnabled,
            boolean friEnabled,
            boolean weekendEnabled
    ) {}

    public record FavoritePlace(
            @NotBlank String name,
            @NotBlank String address,
            Double latitude,
            Double longitude
    ) {}

    public record TransportPref(
            @NotNull TransportType transport,
            int rank
    ) {}

    public record NotificationSetting(
            boolean departAlertEnabled,
            int departAlertMinutes
    ) {}
}
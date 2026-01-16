package app.tamingo.domain.onboarding.dto;

import app.tamingo.domain.onboarding.entity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record OnboardingRequest(
        @Valid @NotNull ActiveTime activeTime,
        @Valid @NotNull @Size(max = 5) List<FavoritePlace> favoritePlaces,
        @Valid @NotNull @Size(min = 3, max = 3, message = "transportPreferences는 3개여야 합니다.")
        List<TransportPref> transportPreferences,
        @Valid @NotNull NotificationSetting notificationSetting
) {
    public record ActiveTime(
            @NotNull
            @Pattern(
                    regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$",
                    message = "시간은 HH:mm 형식이어야 합니다."
            )
            String startTime,

            @NotNull
            @Pattern(
                    regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$",
                    message = "시간은 HH:mm 형식이어야 합니다."
            )
            String endTime,

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
            @Min(value = 1, message = "rank는 1~3이어야 합니다.")
            @Max(value = 3, message = "rank는 1~3이어야 합니다.")
            int rank
    ) {}

    public record NotificationSetting(
            boolean departAlertEnabled,
            AlertMinute departAlertMinutes
    ) {}
}
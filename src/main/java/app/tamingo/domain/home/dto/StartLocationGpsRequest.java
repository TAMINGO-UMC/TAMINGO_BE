package app.tamingo.domain.home.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record StartLocationGpsRequest(

        @NotNull(message = "scheduleId는 필수입니다.")
        Long scheduleId,
        @NotNull(message = "위도는 필수입니다.")
        @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,
        @NotNull(message = "경도는 필수입니다.")
        @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude
) {}

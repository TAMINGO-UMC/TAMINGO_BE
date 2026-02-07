package app.tamingo.domain.transportpreference.dto;

import app.tamingo.domain.transportpreference.entity.TransportType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TransportPreferenceUpdateRequest {

    @NotNull
    private TransportType rank1;

    @NotNull
    private TransportType rank2;

    @NotNull
    private TransportType rank3;
}
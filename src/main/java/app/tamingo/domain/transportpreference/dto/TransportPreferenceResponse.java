package app.tamingo.domain.transportpreference.dto;

import app.tamingo.domain.transportpreference.entity.TransportType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransportPreferenceResponse {

    private TransportType rank1;
    private TransportType rank2;
    private TransportType rank3;
}
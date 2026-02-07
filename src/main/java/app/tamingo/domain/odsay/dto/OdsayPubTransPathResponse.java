package app.tamingo.domain.odsay.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OdsayPubTransPathResponse(
        Result result,
        Error error
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            List<Path> path
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Path(
            Integer pathType,
            List<SubPath> subPath,
            Info info
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Info(
            Integer totalTime,
            Integer totalWalk,
            Integer totalWalkTime,
            Integer totalDistance,
            Integer totalStationCount,
            Integer payment,
            String mapObj
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubPath(
            Integer trafficType,
            Integer distance,
            Integer sectionTime,
            List<Lane> lane,
            Integer stationCount,
            PassStopList passStopList,
            String startName,
            Double startX,
            Double startY,
            String endName,
            Double endX,
            Double endY,
            String way,
            Integer wayCode
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Lane(
            String name,
            Integer subwayCode,
            Integer subwayCityCode,
            String busNo,
            Integer busID
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PassStopList(
            List<Station> stations
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Station(
            Integer stationID,
            String stationName,
            Double x,
            Double y
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Error(
            Integer code,
            String message,
            @JsonProperty("msg")
            String msg
    ) {}
}

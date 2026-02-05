package app.tamingo.domain.home.converter;

import app.tamingo.domain.home.dto.FindRouteResponse;
import app.tamingo.domain.tmap.dto.TmapTransitResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class FindRouteResponseConverter {

    public List<FindRouteResponse.RouteLeg> toRouteLegs(TmapTransitResponse.Itinerary itinerary) {
        if (itinerary == null || itinerary.legs() == null) {
            return List.of();
        }
        return itinerary.legs().stream()
                .map(this::toRouteLeg)
                .filter(Objects::nonNull)
                .toList();
    }

    public Integer toMinutes(Integer seconds) {
        if (seconds == null) {
            return null;
        }
        return (seconds + 59) / 60;
    }

    private FindRouteResponse.RouteLeg toRouteLeg(TmapTransitResponse.Leg leg) {
        if (leg == null) {
            return null;
        }
        FindRouteResponse.TransportMode mode = toTransportMode(leg.mode());
        String startName = leg.start() != null ? leg.start().name() : null;
        String endName = leg.end() != null ? leg.end().name() : null;
        List<String> stations = toStationNames(leg.passStopList());
        String walkDescription = buildWalkDescription(mode, leg.distance(), leg.sectionTime());

        return new FindRouteResponse.RouteLeg(
                mode,
                toMinutes(leg.sectionTime()),
                leg.distance(),
                startName,
                endName,
                leg.route(),
                leg.routeColor(),
                stations,
                walkDescription
        );
    }

    private FindRouteResponse.TransportMode toTransportMode(String mode) {
        if (mode == null) {
            return null;
        }
        return switch (mode.toUpperCase()) {
            case "WALK" -> FindRouteResponse.TransportMode.WALK;
            case "BUS" -> FindRouteResponse.TransportMode.BUS;
            case "SUBWAY" -> FindRouteResponse.TransportMode.SUBWAY;
            default -> null;
        };
    }

    private List<String> toStationNames(TmapTransitResponse.PassStopList passStopList) {
        if (passStopList == null || passStopList.stations() == null) {
            return null;
        }
        return passStopList.stations().stream()
                .map(TmapTransitResponse.Station::stationName)
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildWalkDescription(
            FindRouteResponse.TransportMode mode,
            Integer distanceMeters,
            Integer sectionTimeSeconds
    ) {
        if (mode != FindRouteResponse.TransportMode.WALK) {
            return null;
        }
        if (distanceMeters == null && sectionTimeSeconds == null) {
            return null;
        }
        Integer minutes = toMinutes(sectionTimeSeconds);
        if (distanceMeters == null) {
            return minutes == null ? null : String.format("도보 %d분", minutes);
        }
        if (minutes == null) {
            return String.format("도보 %dm", distanceMeters);
        }
        return String.format("도보 %dm (%d분)", distanceMeters, minutes);
    }
}

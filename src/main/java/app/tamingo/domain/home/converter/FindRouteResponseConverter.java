package app.tamingo.domain.home.converter;

import app.tamingo.domain.home.dto.FindRouteResponse;
import app.tamingo.domain.odsay.dto.OdsayTransitResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class FindRouteResponseConverter {

    private final RouteColorResolver routeColorResolver;

    public FindRouteResponseConverter(RouteColorResolver routeColorResolver) {
        this.routeColorResolver = routeColorResolver;
    }

    public List<FindRouteResponse.RouteLeg> toRouteLegs(OdsayTransitResponse.Itinerary itinerary) {
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

    private FindRouteResponse.RouteLeg toRouteLeg(OdsayTransitResponse.Leg leg) {
        if (leg == null) {
            return null;
        }
        FindRouteResponse.TransportMode mode = toTransportMode(leg.mode());
        String startName = leg.start() != null ? leg.start().name() : null;
        String endName = leg.end() != null ? leg.end().name() : null;
        List<String> stations = toStationNames(leg.passStopList());
        Integer stationCount = stations != null ? stations.size() : null;
        String walkDescription = buildWalkDescription(mode, leg.distance(), leg.sectionTime());
        String routeColor = leg.routeColor() != null
                ? leg.routeColor()
                : routeColorResolver.resolve(mode, leg.route());

        return new FindRouteResponse.RouteLeg(
                mode,
                toMinutes(leg.sectionTime()),
                leg.distance(),
                startName,
                endName,
                leg.route(),
                routeColor,
                stations,
                stationCount,
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

    private List<String> toStationNames(OdsayTransitResponse.PassStopList passStopList) {
        if (passStopList == null || passStopList.stations() == null) {
            return null;
        }
        return passStopList.stations().stream()
                .map(OdsayTransitResponse.Station::stationName)
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

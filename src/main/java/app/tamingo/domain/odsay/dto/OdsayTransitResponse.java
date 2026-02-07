package app.tamingo.domain.odsay.dto;

import java.util.List;

public record OdsayTransitResponse(
        MetaData metaData
) {
    public record MetaData(
            Plan plan
    ) {}

    public record Plan(
            List<Itinerary> itineraries
    ) {}

    public record Itinerary(
            Integer totalTime,
            Integer totalWalkTime,
            Integer totalDistance,
            Integer totalWalkDistance,
            List<Leg> legs
    ) {}

    public record Leg(
            String mode,
            Integer sectionTime,
            Integer distance,
            String route,
            String routeColor,
            String routeId,
            Integer type,
            PlacePoint start,
            PlacePoint end,
            PassStopList passStopList,
            PassShape passShape,
            List<WalkStep> steps
    ) {}

    public record PlacePoint(
            String name,
            Double lon,
            Double lat
    ) {}

    public record PassStopList(
            List<Station> stations
    ) {}

    public record Station(
            String stationID,
            String stationName,
            Double lon,
            Double lat
    ) {}

    public record PassShape(
            String linestring
    ) {}

    public record WalkStep(
            String streetName,
            Integer distance,
            String description,
            String linestring
    ) {}
}

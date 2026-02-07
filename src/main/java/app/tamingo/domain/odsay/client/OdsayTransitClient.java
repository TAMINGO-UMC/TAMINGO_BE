package app.tamingo.domain.odsay.client;

import app.tamingo.common.webclient.ApiClient;
import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.odsay.dto.OdsayPubTransPathResponse;
import app.tamingo.domain.odsay.dto.OdsayTransitResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Component
public class OdsayTransitClient extends ApiClient {

    private final String apiKey;
    private final int searchType;
    private final int searchPathType;
    private final int opt;
    private final ObjectMapper objectMapper;

    public OdsayTransitClient(
            @Qualifier("odsayWebClient") WebClient webClient,
            @Value("${odsay.api-key}") String apiKey,
            @Value("${odsay.search-type:0}") int searchType,
            @Value("${odsay.search-path-type:0}") int searchPathType,
            @Value("${odsay.opt:0}") int opt,
            ObjectMapper objectMapper
    ) {
        super(webClient);
        this.apiKey = apiKey;
        this.searchType = searchType;
        this.searchPathType = searchPathType;
        this.opt = opt;
        this.objectMapper = objectMapper;
    }

    /**
     * 대중교통 경로 조회 (ODSAY Public Transit)
     * startX/startY: 출발지 경도/위도
     * endX/endY: 도착지 경도/위도
     */
    public DirectionResult route(
            double startLat, double startLng,
            double goalLat, double goalLng
    ) {
        try {
            OdsayTransitResponse response = routeResponse(startLat, startLng, goalLat, goalLng);

            if (response == null || response.metaData() == null
                    || response.metaData().plan() == null
                    || response.metaData().plan().itineraries() == null
                    || response.metaData().plan().itineraries().isEmpty()) {
                log.warn("[ODSAY] 경로 결과 없음. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }

            Integer totalTimeSeconds = response.metaData().plan().itineraries().get(0).totalTime();
            if (totalTimeSeconds == null) {
                log.warn("[ODSAY] totalTime 없음. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }

            int totalMinutes = (totalTimeSeconds + 59) / 60;
            return new DirectionResult(totalMinutes, List.of());
        } catch (Exception e) {
            log.error("[ODSAY] 대중교통 경로 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public OdsayTransitResponse routeResponse(
            double startLat, double startLng,
            double goalLat, double goalLng
    ) {
        try {
            if (!isValidCoordinate(startLat, startLng) || !isValidCoordinate(goalLat, goalLng)) {
                log.warn("[ODSAY] 좌표 범위 오류. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }

            String responseBody = get(
                    uriBuilder -> uriBuilder
                            .path("/searchPubTransPathT")
                            .queryParam("SX", startLng)
                            .queryParam("SY", startLat)
                            .queryParam("EX", goalLng)
                            .queryParam("EY", goalLat)
                            .queryParam("apiKey", apiKey)
                            .queryParam("SearchType", searchType)
                            .queryParam("SearchPathType", searchPathType)
                            .queryParam("OPT", opt)
                            .build(),
                    String.class
            );

            if (responseBody == null || responseBody.isBlank()) {
                log.warn("[ODSAY] 응답 비어있음. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }
            JsonNode root;
            try {
                root = objectMapper.readTree(responseBody);
            } catch (Exception parseException) {
                log.error("[ODSAY] 응답 파싱 실패. body={}", responseBody, parseException);
                return null;
            }
            if (root.has("error") && !root.get("error").isNull()) {
                log.warn("[ODSAY] error 응답(raw): {}", root.get("error"));
                return null;
            }
            OdsayPubTransPathResponse odsayResponse = objectMapper.treeToValue(
                    root,
                    OdsayPubTransPathResponse.class
            );
            if (odsayResponse == null) {
                log.warn("[ODSAY] 응답 파싱 실패. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }
            if (odsayResponse.error() != null) {
                OdsayPubTransPathResponse.Error error = odsayResponse.error();
                log.warn("[ODSAY] error 응답: code={}, message={}, msg={}",
                        error.code(), error.message(), error.msg());
                return null;
            }
            if (odsayResponse.result() == null || odsayResponse.result().path() == null
                    || odsayResponse.result().path().isEmpty()) {
                log.warn("[ODSAY] 경로 결과 없음. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }
            return toOdsayTransitResponse(odsayResponse);
        } catch (WebClientResponseException e) {
            log.error("[ODSAY] HTTP error. status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return null;
        } catch (Exception e) {
            log.error("[ODSAY] 대중교통 경로 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    private OdsayTransitResponse toOdsayTransitResponse(OdsayPubTransPathResponse response) {
        if (response == null || response.result() == null
                || response.result().path() == null || response.result().path().isEmpty()) {
            return null;
        }

        List<OdsayTransitResponse.Itinerary> itineraries = response.result().path().stream()
                .map(this::toItinerary)
                .toList();

        return new OdsayTransitResponse(
                new OdsayTransitResponse.MetaData(
                        new OdsayTransitResponse.Plan(itineraries)
                )
        );
    }

    private OdsayTransitResponse.Itinerary toItinerary(OdsayPubTransPathResponse.Path path) {
        if (path == null) {
            return null;
        }

        Integer totalTimeSeconds = minutesToSeconds(path.info() != null ? path.info().totalTime() : null);
        Integer totalWalkTimeSeconds = minutesToSeconds(path.info() != null ? path.info().totalWalkTime() : null);
        Integer totalDistance = path.info() != null ? path.info().totalDistance() : null;
        Integer totalWalkDistance = path.info() != null ? path.info().totalWalk() : null;

        List<OdsayTransitResponse.Leg> legs = path.subPath() == null
                ? List.of()
                : path.subPath().stream().map(this::toLeg).toList();

        return new OdsayTransitResponse.Itinerary(
                totalTimeSeconds,
                totalWalkTimeSeconds,
                totalDistance,
                totalWalkDistance,
                legs
        );
    }

    private OdsayTransitResponse.Leg toLeg(OdsayPubTransPathResponse.SubPath subPath) {
        if (subPath == null) {
            return null;
        }

        String mode = toMode(subPath.trafficType());
        Integer sectionTimeSeconds = minutesToSeconds(subPath.sectionTime());
        String route = resolveRouteName(subPath);
        String routeId = resolveRouteId(subPath);

        OdsayTransitResponse.PlacePoint start = new OdsayTransitResponse.PlacePoint(
                subPath.startName(),
                subPath.startX(),
                subPath.startY()
        );
        OdsayTransitResponse.PlacePoint end = new OdsayTransitResponse.PlacePoint(
                subPath.endName(),
                subPath.endX(),
                subPath.endY()
        );

        OdsayTransitResponse.PassStopList passStopList = null;
        if (subPath.passStopList() != null && subPath.passStopList().stations() != null) {
            List<OdsayTransitResponse.Station> stations = subPath.passStopList().stations().stream()
                    .map(this::toStation)
                    .toList();
            passStopList = new OdsayTransitResponse.PassStopList(stations);
        }

        return new OdsayTransitResponse.Leg(
                mode,
                sectionTimeSeconds,
                subPath.distance(),
                route,
                null,
                routeId,
                subPath.trafficType(),
                start,
                end,
                passStopList,
                null,
                null
        );
    }

    private OdsayTransitResponse.Station toStation(OdsayPubTransPathResponse.Station station) {
        if (station == null) {
            return null;
        }
        String stationId = station.stationID() != null ? String.valueOf(station.stationID()) : null;
        return new OdsayTransitResponse.Station(
                stationId,
                station.stationName(),
                station.x(),
                station.y()
        );
    }

    private String toMode(Integer trafficType) {
        if (trafficType == null) {
            return null;
        }
        return switch (trafficType) {
            case 1 -> "SUBWAY";
            case 2 -> "BUS";
            case 3 -> "WALK";
            default -> null;
        };
    }

    private String resolveRouteName(OdsayPubTransPathResponse.SubPath subPath) {
        if (subPath == null || subPath.lane() == null || subPath.lane().isEmpty()) {
            return null;
        }
        OdsayPubTransPathResponse.Lane lane = subPath.lane().get(0);
        if (lane == null) {
            return null;
        }
        if (lane.name() != null) {
            return lane.name();
        }
        return lane.busNo();
    }

    private String resolveRouteId(OdsayPubTransPathResponse.SubPath subPath) {
        if (subPath == null || subPath.lane() == null || subPath.lane().isEmpty()) {
            return null;
        }
        OdsayPubTransPathResponse.Lane lane = subPath.lane().get(0);
        if (lane == null) {
            return null;
        }
        if (lane.busID() != null) {
            return String.valueOf(lane.busID());
        }
        if (lane.subwayCode() != null) {
            return String.valueOf(lane.subwayCode());
        }
        return null;
    }

    private Integer minutesToSeconds(Integer minutes) {
        if (minutes == null) {
            return null;
        }
        if (minutes < 0) {
            return minutes;
        }
        return minutes * 60;
    }

    private boolean isValidCoordinate(double lat, double lng) {
        if (Double.isNaN(lat) || Double.isNaN(lng) || Double.isInfinite(lat) || Double.isInfinite(lng)) {
            return false;
        }
        return lat >= -90.0 && lat <= 90.0 && lng >= -180.0 && lng <= 180.0;
    }
}

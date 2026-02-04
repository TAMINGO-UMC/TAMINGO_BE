package app.tamingo.domain.tmap.client;

import app.tamingo.common.webclient.ApiClient;
import app.tamingo.domain.home.dto.DirectionResult;
import app.tamingo.domain.tmap.dto.TmapTransitRequest;
import app.tamingo.domain.tmap.dto.TmapTransitResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Component
public class TmapTransitClient extends ApiClient {

    private final String format;
    private final int lang;
    private final int count;

    public TmapTransitClient(
            @Qualifier("tmapWebClient") WebClient webClient,
            @Value("${tmap.format:json}") String format,
            @Value("${tmap.lang:0}") int lang,
            @Value("${tmap.count:1}") int count
    ) {
        super(webClient);
        this.format = format;
        this.lang = lang;
        this.count = count;
    }

    /**
     * 대중교통 경로 조회 (TMAP Transit)
     * startX/startY: 출발지 경도/위도
     * endX/endY: 도착지 경도/위도
     */
    public DirectionResult route(
            double startLat, double startLng,
            double goalLat, double goalLng
    ) {
        try {
            TmapTransitResponse response = routeResponse(startLat, startLng, goalLat, goalLng);

            if (response == null || response.metaData() == null
                    || response.metaData().plan() == null
                    || response.metaData().plan().itineraries() == null
                    || response.metaData().plan().itineraries().isEmpty()) {
                log.warn("[TMAP] 경로 결과 없음. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }

            Integer totalTimeSeconds = response.metaData().plan().itineraries().get(0).totalTime();
            if (totalTimeSeconds == null) {
                log.warn("[TMAP] totalTime 없음. start=({},{}), goal=({}, {})",
                        startLat, startLng, goalLat, goalLng);
                return null;
            }

            int totalMinutes = (totalTimeSeconds + 59) / 60;
            return new DirectionResult(totalMinutes, List.of());
        } catch (Exception e) {
            log.error("[TMAP] 대중교통 경로 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    public TmapTransitResponse routeResponse(
            double startLat, double startLng,
            double goalLat, double goalLng
    ) {
        try {
            TmapTransitRequest body = new TmapTransitRequest(
                    String.valueOf(startLng),
                    String.valueOf(startLat),
                    String.valueOf(goalLng),
                    String.valueOf(goalLat),
                    count,
                    lang,
                    format
            );

            return post(
                    uriBuilder -> uriBuilder.path("/routes").build(),
                    body,
                    TmapTransitResponse.class
            );
        } catch (Exception e) {
            log.error("[TMAP] 대중교통 경로 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }
}

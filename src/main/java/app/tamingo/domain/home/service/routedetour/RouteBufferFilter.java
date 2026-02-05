package app.tamingo.domain.home.service.routedetour;

import app.tamingo.domain.home.dto.Location;
import app.tamingo.domain.home.service.geoutil.GeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

// 경로 버퍼 필터, 할일 위치가 경로 근처에 있는지 판단
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteBufferFilter {

    private final GeoService geoService;

    /**
     * 할일 위치가 경로 근처에 있는지 확인
     * 
     * @param todoLat  할일 위도
     * @param todoLng  할일 경도
     * @param polyline 경로 좌표 리스트
     * @param bufferKm 버퍼 거리
     * @return 경로 근처에 있으면 true
     */
    public boolean isNearRoute(
            double todoLat, double todoLng,
            List<Location> polyline,
            double bufferKm) {

        if (polyline == null || polyline.isEmpty()) {
            return false;
        }

        // polyline 상의 모든 점과 할일 위치 간 거리를 계산, 하나라도 버퍼 범위 내에 있으면 true
        boolean isNear = polyline.stream()
                .anyMatch(point -> geoService.isWithin(
                        todoLat, todoLng,
                        point.latitude(), point.longitude(),
                        bufferKm));

        log.debug("Route buffer check: todoLat={}, todoLng={}, bufferKm={}, isNear={}",
                todoLat, todoLng, bufferKm, isNear);

        return isNear;
    }
}

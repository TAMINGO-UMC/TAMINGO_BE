package app.tamingo.domain.home.service.geoutil;

import org.springframework.stereotype.Component;

@Component
public class GeoService {

    /**
     * 지구 반지름
     */
    private static final int EARTH_RADIUS_KM = 6371;

    /**
     * Haversine 공식의 중심각 계산 계수
     */
    private static final int HAVERSINE_COEFFICIENT = 2;

    /**
     * 두 위도/경도 좌표 간의 거리 계산, km 단위
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public double distanceKm(
            double lat1, double lon1,
            double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / HAVERSINE_COEFFICIENT) * Math.sin(latDistance / HAVERSINE_COEFFICIENT)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / HAVERSINE_COEFFICIENT) * Math.sin(lonDistance / HAVERSINE_COEFFICIENT);

        double c = HAVERSINE_COEFFICIENT * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 두 위도/경도 좌표가 특정 거리 이내에 있는지 여부 판단
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @param km
     * @return
     */

    public boolean isWithin(
            double lat1, double lon1,
            double lat2, double lon2,
            double km) {
        return distanceKm(lat1, lon1, lat2, lon2) <= km;
    }
}

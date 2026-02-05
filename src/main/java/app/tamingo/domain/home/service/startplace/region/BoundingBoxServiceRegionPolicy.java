package app.tamingo.domain.home.service.startplace.region;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoundingBoxServiceRegionPolicy implements ServiceRegionPolicy {

    private final ServiceRegionProperties properties;

    @Override
    public boolean isAllowed(double latitude, double longitude) {
        if (!properties.isConfigured()) {
            return true;
        }
        return latitude >= properties.getMinLat()
                && latitude <= properties.getMaxLat()
                && longitude >= properties.getMinLng()
                && longitude <= properties.getMaxLng();
    }
}

package app.tamingo.domain.home.service.startplace.region;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "service-region")
public class ServiceRegionProperties {
    private Double minLat;
    private Double maxLat;
    private Double minLng;
    private Double maxLng;

    public boolean isConfigured() {
        return minLat != null && maxLat != null && minLng != null && maxLng != null;
    }
}

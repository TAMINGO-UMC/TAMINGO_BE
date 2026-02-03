package app.tamingo.domain.home.service.startplace.region;

public interface ServiceRegionPolicy {
    boolean isAllowed(double latitude, double longitude);
}

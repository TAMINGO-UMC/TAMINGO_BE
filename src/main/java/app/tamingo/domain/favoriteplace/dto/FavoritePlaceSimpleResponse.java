package app.tamingo.domain.favoriteplace.dto;

import app.tamingo.domain.favoriteplace.entity.FavoritePlaceStandard;

public record FavoritePlaceSimpleResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {
    public static FavoritePlaceSimpleResponse from(FavoritePlaceStandard entity) {
        return new FavoritePlaceSimpleResponse(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }
}

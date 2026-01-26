package app.tamingo.domain.favoriteplace.dto;

import app.tamingo.domain.favoriteplace.entity.FavoritePlaceStandard;

public record FavoritePlaceResponse (
    Long id,
    String name,
    String address,
    Double latitude,
    Double longitude
) {
    public static FavoritePlaceResponse from(FavoritePlaceStandard entity) {
        return new FavoritePlaceResponse(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }
}



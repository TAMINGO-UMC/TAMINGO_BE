package app.tamingo.domain.favoriteplace.dto;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;

public record FavoritePlaceResponse (
    Long id,
    String name,
    String address,
    Double latitude,
    Double longitude
) {
    public static FavoritePlaceResponse from(FavoritePlace entity) {
        return new FavoritePlaceResponse(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }
}



package app.tamingo.domain.favoriteplace.dto;

import app.tamingo.domain.favoriteplace.entity.FavoritePlace;

public record FavoritePlaceSimpleResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {
    public static FavoritePlaceSimpleResponse from(FavoritePlace entity) {
        return new FavoritePlaceSimpleResponse(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getLatitude(),
                entity.getLongitude()
        );
    }
}

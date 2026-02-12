package app.tamingo.domain.favoriteplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class FavoritePlaceRequest {

    public record SaveDto(
            @NotBlank(message = "장소 이름을 입력해 주세요.")
            String name,

            @NotBlank(message = "주소를 입력해 주세요.")
            String address,

            Double latitude,
            Double longitude,
            Boolean isAiSuggested
    ) {
        public SaveDto{
            if (isAiSuggested == null) {
                isAiSuggested = false;
            }
        }
    }

    @Schema(name = "FavoritePlaceUpdateDto")
    public record UpdateDto(
            @NotBlank(message = "장소 이름을 입력해 주세요.")
            String name,

            @NotBlank(message = "주소를 입력해 주세요.")
            String address,

            Double latitude,
            Double longitude
    ) {}
}

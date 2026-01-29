package app.tamingo.domain.favoriteplace.exception;

import app.tamingo.common.response.BaseCode;
import app.tamingo.common.response.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FavoritePlaceErrorCode implements BaseCode {

    FAVORITE_PLACE_NOT_FOUND(HttpStatus.NOT_FOUND, "PLACE-001", "해당 장소를 찾을 수 없습니다."),
    FAVORITE_PLACE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "PLACE-002", "이미 등록된 장소명 또는 주소입니다."),
    FAVORITE_PLACE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PLACE-003", "자주 가는 장소의 최대 등록 개수를 초과했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

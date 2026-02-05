package app.tamingo.domain.todo.dto;

import jakarta.validation.constraints.NotNull;

public record TodoCheckRequest(
        @NotNull(message = "체크 여부는 필수입니다.")
        Boolean isChecked
) {
}

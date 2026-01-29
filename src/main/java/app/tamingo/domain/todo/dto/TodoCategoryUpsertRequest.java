package app.tamingo.domain.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoCategoryUpsertRequest(

        @NotBlank(message = "카테고리 이름은 필수입니다.")
        @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다.")
        String name,

        @NotBlank(message = "아이콘 코드는 필수입니다.")
        @Size(max = 50, message = "아이콘 코드는 50자 이하여야 합니다.")
        String iconCode,

        @NotBlank(message = "색상 코드는 필수입니다.")
        @Size(max = 20, message = "색상 코드는 20자 이하여야 합니다.")
        String colorCode
) {}

package app.tamingo.domain.todo.dto;

import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.entity.TodoCategory;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateTodoRequest(
        @NotBlank(message = "할 일 제목은 필수입니다.")
        String title,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate targetDate, // null이면 '날짜 미지정'

        Long todoCategoryId,
        String placeName,
        String address,
        Double latitude,
        Double longitude,
        Integer duration,
        RepeatType repeatType,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate repeatEndDate,
        AiInferenceContent aiSource
) {
    public record AiInferenceContent(
            String aiSuggestedCategoryName,
            String aiSuggestedPlaceName,
            Integer aiSuggestedDuration
    ) {}

    public Todo toEntity(User user, TodoCategory category) {
        return Todo.of(
                user,
                category,
                this.title(),
                this.targetDate(),
                this.placeName(),
                this.address(),
                this.latitude(),
                this.longitude(),
                this.duration(),
                this.repeatType(),
                this.repeatEndDate()
        );
    }
}

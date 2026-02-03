package app.tamingo.domain.home.dto;

import java.time.LocalDate;
import java.util.List;

public record GapSuggestionRunResponse(
        LocalDate targetDate,
        int totalUsers,
        int successUsers,
        List<Long> failedUserIds,
        Long userId
) {
}

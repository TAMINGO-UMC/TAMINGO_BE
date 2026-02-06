package app.tamingo.test.controller;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ApiResponse;
import app.tamingo.common.response.SuccessCode;
import app.tamingo.domain.home.dto.GapSuggestionRunResponse;
import app.tamingo.domain.home.service.gapsuggestion.GapSuggestionBatchService;
import app.tamingo.domain.home.service.routedetour.RouteDetourSuggestionService;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.test.initializer.TestDataInitializer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@Profile("dev")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/home/suggestions")
public class HomeSuggestionTestController {

    private static final ZoneId TARGET_ZONE = ZoneId.of("Asia/Seoul");

    private final GapSuggestionBatchService gapSuggestionBatchService;
    private final RouteDetourSuggestionService routeDetourSuggestionService;
    private final UserRepository userRepository;

    @PostMapping("/gap")
    public ApiResponse<GapSuggestionRunResponse> runGapSuggestion(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User user = resolveUser(userId);
        LocalDate targetDate = (date != null) ? date : LocalDate.now(TARGET_ZONE);
        GapSuggestionRunResponse response = gapSuggestionBatchService.runForUser(user, targetDate);
        return ApiResponse.onSuccess(response, SuccessCode.OK);
    }

    @PostMapping("/route-detour")
    public ApiResponse<Integer> runRouteDetourSuggestion(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User user = resolveUser(userId);
        LocalDate targetDate = (date != null) ? date : LocalDate.now(TARGET_ZONE);
        int count = routeDetourSuggestionService.generateRouteDetourSuggestions(user, targetDate);
        return ApiResponse.onSuccess(count, SuccessCode.OK);
    }

    private User resolveUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        }
        return userRepository.findByEmail(TestDataInitializer.TEST_EMAIL)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }
}

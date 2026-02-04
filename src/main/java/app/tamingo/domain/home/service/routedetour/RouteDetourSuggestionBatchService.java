package app.tamingo.domain.home.service.routedetour;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RouteDetourSuggestionBatchService {

    private final UserRepository userRepository;
    private final RouteDetourSuggestionService routeDetourSuggestionService;

    public int runForAllUsers() {
        return runForAllUsers(LocalDate.now());
    }

    public int runForAllUsers(LocalDate targetDate) {
        List<User> users = userRepository.findAll();
        int total = 0;
        for (User user : users) {
            total += routeDetourSuggestionService.generateRouteDetourSuggestions(user, targetDate);
        }
        log.info("[HOME][DETOUR] 전체 사용자 경로 연계 추천 완료 date={}, count={}", targetDate, total);
        return total;
    }
}

package app.tamingo.domain.schedule.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.favoriteplace.repository.FavoritePlaceStandardRepository;
import app.tamingo.domain.schedule.dto.RecommendTodoResponse;
import app.tamingo.domain.schedule.repository.ScheduleRepository;
import app.tamingo.domain.todo.dto.TodoSummaryResponse;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.repository.TodoRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceContextService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final ScheduleRepository scheduleRepository;
    private final FavoritePlaceStandardRepository favoritePlaceStandardRepository;

    public RecommendTodoResponse getPlaceContext(Long userId, String placeName, Double latitude, Double longitude){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // [List A] 2km 범위 할 일 조회
        double radius = 0.02;
        Double minLat = latitude - radius;
        Double maxLat = latitude + radius;
        Double minLon = longitude - radius;
        Double maxLon = longitude + radius;

        List<Todo> nearbyEntityList = todoRepository.findNearbyTodos(
                    userId, latitude, longitude, minLat, maxLat, minLon, maxLon
        );

        List<TodoSummaryResponse> nearbyTodos = nearbyEntityList.stream()
                .map(TodoSummaryResponse::from)
                .toList();

        // [List B] 조건기반 할 일 조회(이번주 + 날짜 미지정)
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<Todo> candidateEntityList = todoRepository.findCandidateTodos(
                userId,startOfWeek, endOfWeek);

        Set<Long> nearbyTodoIds = nearbyEntityList.stream()
                .map(Todo::getId)
                .collect(Collectors.toSet());

        List<TodoSummaryResponse> candidateTodos = candidateEntityList.stream()
                .filter(todo -> !nearbyTodoIds.contains(todo.getId()))
                .map(TodoSummaryResponse::from)
                .toList();

        // 자주 가는 장소 추천 여부
        boolean isFavoriteRecommendation = checkFavoriteRecommendation(user, placeName);

        return RecommendTodoResponse.builder()
                .nearbyTodos(nearbyTodos)
                .candidateTodos(candidateTodos)
                .isFavoriteRecommendation(isFavoriteRecommendation)
                .build();
    }

    private boolean checkFavoriteRecommendation(User user, String placeName){
        if(placeName == null || placeName.isBlank()){
            return false;
        }

        boolean alreadyExists = favoritePlaceStandardRepository.existsByUserAndName(user, placeName);
        if(alreadyExists){
            return false;
        }

        int scheduleCount = scheduleRepository.countByUserAndPlaceName(user, placeName);
        int todoCount = todoRepository.countByUserAndPlaceNameAndIsLocationConfirmedTrue(user, placeName);

        return (scheduleCount + todoCount) >=3;
    }

}

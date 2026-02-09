package app.tamingo.domain.home.repository;

import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SuggestionLearningRepository extends JpaRepository<SuggestionLearning,Long> {

    @Query("""
        select case when count(sl) > 0 then true else false end
        from SuggestionLearning sl
        where sl.user = :user
            and sl.suggestionType = :suggestionType
            and sl.linkedTodoId = :linkedTodoId
            and sl.schedule = :schedule
    """)
    boolean existsLinkedSuggestion(
            User user,
            SuggestionType suggestionType,
            Long linkedTodoId,
            Schedule schedule
    );

    @Query("""
        select sl from SuggestionLearning sl 
        where sl.user = :user 
            and sl.startTime >= :now
            and sl.suggestionType = :type
         order by sl.startTime asc
    """)
    List<SuggestionLearning> findAllSLFromNow(
            @Param("user") User user,
            @Param("now") LocalDateTime now,
            @Param("type") SuggestionType type
            );

    List<SuggestionLearning> findBySchedule(Schedule schedule);

    // 8번 알림용 - 시간이 가장 긴 틈새시간의 할 일
    @Query("""
    select sl from SuggestionLearning sl
    where sl.user = :user
      and sl.suggestionType = :type
      and sl.startTime >= :startOfDay
    order by sl.duration desc
    limit 1
    """)
    Optional<SuggestionLearning> findBestGapForNotification(
            @Param("user") User user,
            @Param("type") SuggestionType type,
            @Param("startOfDay") LocalDateTime startOfDay
    );


     // 11번 알림용 - 연계
    @Query("""
    select sl from SuggestionLearning sl
    where sl.user = :user
      and sl.suggestionType = :type
      and sl.startTime >= :startOfDay
    order by sl.startTime desc
    limit 1
    """)
    Optional<SuggestionLearning> findBestRouteSuggestion(
            @Param("user") User user,
            @Param("type") SuggestionType type,
            @Param("startOfDay") LocalDateTime startOfDay
    );
}

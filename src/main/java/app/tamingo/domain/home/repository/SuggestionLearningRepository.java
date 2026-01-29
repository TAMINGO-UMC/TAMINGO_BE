package app.tamingo.domain.home.repository;

import app.tamingo.domain.home.entity.SuggestionLearning;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SuggestionLearningRepository extends JpaRepository<SuggestionLearning,Long> {


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
}

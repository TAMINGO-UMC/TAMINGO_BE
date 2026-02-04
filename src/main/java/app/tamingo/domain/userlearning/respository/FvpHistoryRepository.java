package app.tamingo.domain.userlearning.respository;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.FvpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface FvpHistoryRepository extends JpaRepository<FvpHistory, Long> {

    void deleteByUser(User user);

    @Query("SELECT COUNT(f) FROM FvpHistory f " +
            "WHERE f.user = :user " +
            "AND f.name = :name " +
            "AND f.createdAt >= :monday")
    int countWeeklyVisits(
            @Param("user") User user,
            @Param("name") String name,
            @Param("monday") LocalDateTime monday
    );
}

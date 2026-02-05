package app.tamingo.domain.userlearning.repository;

import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    void deleteByUser(User user);

    @Query("""
        select e
        from ErrorLog e
        where e.user = :user
        order by e.createdAt desc
        limit :num
    """)
    List<ErrorLog> findLatestByUserByNum(
            @Param("num") int num,
            @Param("user") User user);
}

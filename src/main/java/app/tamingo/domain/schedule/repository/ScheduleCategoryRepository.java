package app.tamingo.domain.schedule.repository;

import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleCategoryRepository extends JpaRepository<ScheduleCategory,Long> {

    // 유저 카테고리 조회
    List<ScheduleCategory> findAllByUser(User user);

}

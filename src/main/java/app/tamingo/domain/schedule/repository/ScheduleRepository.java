package app.tamingo.domain.schedule.repository;

import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.user.entity.User;
import jakarta.validation.constraints.Null;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 자주 가는 장소 추천 로직
    // 해당 유저가 이 장소명으로 등록한 일정이 몇 개인지 카운트
    int countByUserAndPlaceName(User user, String placeName);

    // 키워드 포함 검색 (Top 20)
    // 과거에 저장한 기록 서치
    List<Schedule> findTop20ByUserAndTitleContainingOrderByStartTimeDesc(User user, String titleKeyword);

    // 최근 기록 조회 (Top 20)
    // 검색어와 상관없이 사용자의 최근 생활 반경 파악
    List<Schedule> findTop20ByUserOrderByStartTimeDesc(User user);

    // 특정 날짜 일정 목록 조회 (정렬: 시작시간 오름차순 -> 종료시간 오름차순)
    List<Schedule> findAllByUserAndStartTimeBetweenOrderByStartTimeAscEndTimeAsc(
            User user,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    List<Schedule> findAllByStartTimeBetween(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("""
    select s
    from Schedule s
    where s.user = :user
      and s.startTime < :startTime
    order by s.startTime desc
    limit 1
    """)
    Optional<Schedule> findBeforeStartTime(
            User user,
            LocalDateTime startTime
    );

    //스케줄 카테고리가 사용 중이면 삭제 불가
    boolean existsByUserAndScheduleCategory(User user, ScheduleCategory scheduleCategory);

    @Query("""
   select s
   from Schedule s
   where s.user = :user
       and s.startTime >= :startOfDay
       and s.startTime < :endOfDay
   order by s.startTime asc
   """)
    List<Schedule> findAllToDaySchedules(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );


    Optional<Schedule> findByIdAndUser(Long scheduleId, User user);
}

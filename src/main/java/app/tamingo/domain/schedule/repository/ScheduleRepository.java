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
    //주간 범위 스케줄 조회
    List<Schedule> findAllByUserIdAndStartTimeBetween(Long userId, LocalDateTime startInclusive, LocalDateTime endInclusive);

    /**
     * NO_SHOW 확정 대상 조회
     * - cutoffTime 이전에 시작한 일정 중
     *   1) schedule_result가 아예 없거나
     *   2) schedule_result.status = PENDING 인 경우
     */
    @Query("""
        select s
        from Schedule s
        left join ScheduleResult sr on sr.scheduleId = s.id
        where s.startTime <= :cutoffTime
          and (sr is null or sr.status = 'PENDING')
    """)
    List<Schedule> findNoShowCandidates(@Param("cutoffTime") LocalDateTime cutoffTime);


    List<Schedule> findAllByUserIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long userId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    // [Nearby] 반경 2km 이내 + 현재 시간 이후의 일정 조회
    @Query(value = """
        SELECT * FROM schedule s 
        WHERE s.user_id = :userId 
        AND s.start_time >= :now
        AND s.latitude BETWEEN :minLat AND :maxLat 
        AND s.longitude BETWEEN :minLon AND :maxLon
        AND (6371 * acos(cos(radians(:latitude)) * cos(radians(s.latitude)) 
        * cos(radians(s.longitude) - radians(:longitude)) 
        + sin(radians(:latitude)) * sin(radians(s.latitude)))) <= 2.0
        ORDER BY s.start_time ASC
        """, nativeQuery = true)
    List<Schedule> findNearbySchedules(
            @Param("userId") Long userId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon,
            @Param("now") LocalDateTime now
    );

    // [Weekly] 특정 기간(오늘 ~ 7일 후) 사이 일정 조회
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.user.id = :userId 
        AND s.startTime BETWEEN :startDate AND :endDate 
        ORDER BY s.startTime ASC
    """)
    List<Schedule> findSchedulesInPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


    Optional<Schedule> findByIdAndUser(Long scheduleId, User user);
}

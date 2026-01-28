package app.tamingo.domain.schedule.repository;

import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.schedule.entity.ScheduleCategory;
import app.tamingo.domain.user.entity.User;
import jakarta.validation.constraints.Null;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

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

    //스케줄 카테고리가 사용 중이면 삭제 불가
    boolean existsByUserAndScheduleCategory(User user, ScheduleCategory scheduleCategory);

}

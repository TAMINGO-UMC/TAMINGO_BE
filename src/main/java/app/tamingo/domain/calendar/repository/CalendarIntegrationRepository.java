package app.tamingo.domain.calendar.repository;

import app.tamingo.domain.calendar.entity.CalendarIntegration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

//유저별 애플 연동 조회용
public interface CalendarIntegrationRepository extends JpaRepository<CalendarIntegration, Long> {

    //유저당 1개 연동이므로 userId로 조회
    Optional<CalendarIntegration> findByUserId(Long userId);
}

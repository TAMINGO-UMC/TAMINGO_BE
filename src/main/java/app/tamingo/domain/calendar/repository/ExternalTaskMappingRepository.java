package app.tamingo.domain.calendar.repository;

import app.tamingo.domain.calendar.entity.ExternalTaskMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExternalTaskMappingRepository extends JpaRepository<ExternalTaskMapping, Long> {

    //schedule 수정 시 매핑 조회
    Optional<ExternalTaskMapping> findByScheduleId(Long scheduleId);

    //sync 시 (integrationId, externalEventUid)로 매핑 조회 (calendarEvent 조인)
    @Query("""
    select m
    from ExternalTaskMapping m
    join m.calendarEvent e
    where m.integration.id = :integrationId
    and e.externalEventUid = :externalEventUid
    """)
    Optional<ExternalTaskMapping> findByIntegrationAndUid(
            @Param("integrationId") Long integrationId,
            @Param("externalEventUid") String externalEventUid
    );
}

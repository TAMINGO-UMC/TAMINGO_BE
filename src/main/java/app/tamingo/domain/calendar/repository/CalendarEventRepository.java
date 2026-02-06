package app.tamingo.domain.calendar.repository;

import app.tamingo.domain.calendar.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    //(integrationId, externalEventUid)로 CalendarEvent 조회
    @Query("""
        select ce
        from CalendarEvent ce
        where ce.integration.id = :integrationId
          and ce.externalEventUid = :externalEventUid
    """)
    Optional<CalendarEvent> findByIntegrationAndUid(
            @Param("integrationId") Long integrationId,
            @Param("externalEventUid") String externalEventUid
    );
}

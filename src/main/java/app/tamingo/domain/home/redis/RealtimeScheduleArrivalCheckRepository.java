package app.tamingo.domain.home.redis;

import org.springframework.data.repository.CrudRepository;

public interface RealtimeScheduleArrivalCheckRepository
        extends CrudRepository<RealtimeScheduleArrivalCheck, String> {
}

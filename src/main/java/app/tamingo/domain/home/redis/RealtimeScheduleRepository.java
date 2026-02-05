package app.tamingo.domain.home.redis;

import org.springframework.data.repository.CrudRepository;

public interface RealtimeScheduleRepository
        extends CrudRepository<RealtimeSchedule, String> {
}

package app.tamingo.domain.home.redis;

import org.springframework.data.repository.CrudRepository;

public interface RealtimeActiveScheduleRepository
        extends CrudRepository<RealtimeActiveSchedule, String> {
}

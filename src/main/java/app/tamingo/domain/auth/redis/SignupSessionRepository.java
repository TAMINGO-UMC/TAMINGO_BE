package app.tamingo.domain.auth.redis;

import org.springframework.data.repository.CrudRepository;

public interface SignupSessionRepository extends CrudRepository<SignupSession, String> {
}
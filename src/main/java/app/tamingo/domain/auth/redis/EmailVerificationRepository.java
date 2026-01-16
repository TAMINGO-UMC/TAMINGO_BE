package app.tamingo.domain.auth.redis;

import app.tamingo.domain.auth.entity.EmailVerification;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {
}
package app.tamingo.domain.terms.repository;

import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {
    Optional<Terms> findTopByCodeOrderByEffectiveAtDesc(TermsCode code);
}
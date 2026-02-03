package app.tamingo.domain.onboarding.repository;

import app.tamingo.domain.onboarding.entity.TransportPreference;
import app.tamingo.domain.onboarding.entity.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportPreferenceRepository extends JpaRepository<TransportPreference, Long> {

    List<TransportPreference> findAllByUserIdOrderByRankAsc(Long userId);

    boolean existsByUserIdAndTransport(Long userId, TransportType transport);

    boolean existsByUserIdAndRank(Long userId, int rank);

    void deleteAllByUserId(Long userId);
}
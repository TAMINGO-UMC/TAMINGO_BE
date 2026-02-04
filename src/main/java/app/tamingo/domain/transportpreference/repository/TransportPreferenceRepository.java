package app.tamingo.domain.transportpreference.repository;

import app.tamingo.domain.transportpreference.entity.TransportPreference;
import app.tamingo.domain.transportpreference.entity.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportPreferenceRepository extends JpaRepository<TransportPreference, Long> {

    List<TransportPreference> findAllByUserIdOrderByRankAsc(Long userId);

    void deleteAllByUserId(Long userId);
}
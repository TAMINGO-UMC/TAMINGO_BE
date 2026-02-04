package app.tamingo.domain.transportpreference.repository;

import app.tamingo.domain.transportpreference.entity.TransportPreference;
import app.tamingo.domain.transportpreference.entity.TransportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransportPreferenceRepository extends JpaRepository<TransportPreference, Long> {

    List<TransportPreference> findAllByUserIdOrderByRankAsc(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from TransportPreference tp where tp.user.id = :userId")
    void deleteAllByUserId(Long userId);
}
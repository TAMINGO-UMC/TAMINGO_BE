package app.tamingo.domain.terms.repository;

import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    // 약관 목록 조회 (필수 먼저, 같은 그룹이면 최신 적용일자 먼저)
    List<Terms> findAllByOrderByRequiredDescEffectiveAtDesc();

    // 약관 개별 조회 (코드로 조회)
    Optional<Terms> findByCode(TermsCode code);

    boolean existsByCode(TermsCode code);
}
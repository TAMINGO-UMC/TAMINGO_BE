package app.tamingo.domain.terms.repository;

import app.tamingo.domain.terms.entity.UserTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {

    // 유저가 동의한 약관 목록 조회
    List<UserTermsAgreement> findByUserId(Long userId);
}
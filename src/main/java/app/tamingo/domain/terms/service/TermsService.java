package app.tamingo.domain.terms.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.terms.dto.TermsDetailResponse;
import app.tamingo.domain.terms.dto.TermsSummaryResponse;
import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;
import app.tamingo.domain.terms.exception.TermsErrorCode;
import app.tamingo.domain.terms.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;

    public List<TermsSummaryResponse> getTermsList() {
        return termsRepository.findAllByOrderByIsRequiredDescEffectiveAtDesc()
                .stream()
                .map(TermsSummaryResponse::from)
                .toList();
    }

    public TermsDetailResponse getTermsDetail(TermsCode code) {
        Terms terms = termsRepository.findByCode(code)
                .orElseThrow(() -> new CustomException(TermsErrorCode.TERMS_NOT_FOUND));
        return TermsDetailResponse.from(terms);
    }
}
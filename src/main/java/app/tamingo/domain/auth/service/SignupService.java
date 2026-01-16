package app.tamingo.domain.auth.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.auth.redis.SignupSession;
import app.tamingo.domain.auth.redis.SignupSessionRepository;
import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;
import app.tamingo.domain.terms.repository.TermsRepository;
import app.tamingo.domain.terms.repository.UserTermsAgreementRepository;
import app.tamingo.domain.terms.entity.UserTermsAgreement;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SignupService {

    private static final long SIGNUP_TTL_SEC = 900L; // 15분

    private final SignupSessionRepository signupSessionRepository;
    private final TermsRepository termsRepository;
    private final UserRepository userRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;

    private final EmailVerificationService emailVerificationService;

    // 1. 회원가입 세션 생성 (약관 동의 포함)
    public String createSignupSession(Map<TermsCode, Boolean> agreedTerms) {

        Map<TermsCode, Boolean> safeMap = toSafeEnumMap(agreedTerms);

        // 필수 약관 동의 검증
        List<Terms> requiredTerms = termsRepository.findAllByIsRequiredTrue();
        for (Terms t : requiredTerms) {
            if (!Boolean.TRUE.equals(safeMap.get(t.getCode()))) {
                throw new CustomException(ErrorCode.REQUIRED_TERMS_NOT_AGREED);
            }
        }

        // 세션 생성
        String sessionId = UUID.randomUUID().toString();
        SignupSession session = SignupSession.create(sessionId, safeMap, SIGNUP_TTL_SEC);
        signupSessionRepository.save(session);

        return sessionId;
    }

    // 2. 이메일 인증번호 발송
    public void sendEmailCode(String signupSessionId, String email) {
        SignupSession session = getSessionOrThrow(signupSessionId);

        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 이미 가입된 이메일일 경우
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 인증번호 발송
        emailVerificationService.sendCode(email);

        session.setEmail(email);
        signupSessionRepository.save(session);
    }

    // 3. 이메일 인증번호 확인
    public void verifyEmailCode(String signupSessionId, String email, String code) {
        SignupSession session = getSessionOrThrow(signupSessionId);

        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 세션에 저장된 이메일과 요청 이메일이 다를 경우
        if (session.getEmail() == null || !session.getEmail().equals(email)) {
            throw new CustomException(ErrorCode.SIGNUP_EMAIL_NOT_MATCHED);
        }

        // 인증번호 확인 실패 시
        boolean ok = emailVerificationService.verifyCode(email, code);
        if (!ok) {
            throw new CustomException(ErrorCode.SIGNUP_EMAIL_CODE_INVALID);
        }

        session.markEmailVerified();
        signupSessionRepository.save(session);
    }

    // 4. 아이디 생성 (회원가입 완료)
    public Long completeSignup(String signupSessionId, String nickname) {
        SignupSession session = getSessionOrThrow(signupSessionId);

        if (!session.isEmailVerified()) {
            throw new CustomException(ErrorCode.SIGNUP_EMAIL_NOT_VERIFIED);
        }

        String email = session.getEmail();
        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 유저 생성
        User user = User.create(email, nickname);
        userRepository.save(user);

        // 약관 동의 여부 저장
        List<Terms> currentTerms = termsRepository.findAllByOrderByIsRequiredDescEffectiveAtDesc();
        Map<TermsCode, Boolean> agreedMap = toSafeEnumMap(session.getAgreedTerms());

        for (Terms t : currentTerms) {
            boolean agreed = Boolean.TRUE.equals(agreedMap.get(t.getCode()));
            userTermsAgreementRepository.save(UserTermsAgreement.agree(user, t, agreed));
        }

        // 세션 삭제
        signupSessionRepository.deleteById(signupSessionId);

        return user.getId();
    }

    private SignupSession getSessionOrThrow(String signupSessionId) {
        if (signupSessionId == null || signupSessionId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return signupSessionRepository.findById(signupSessionId)
                .orElseThrow(() -> new CustomException(ErrorCode.SIGNUP_SESSION_NOT_FOUND));
    }

    private Map<TermsCode, Boolean> toSafeEnumMap(Map<TermsCode, Boolean> source) {
        Map<TermsCode, Boolean> safe = new EnumMap<>(TermsCode.class);
        if (source != null) safe.putAll(source);
        return safe;
    }
}
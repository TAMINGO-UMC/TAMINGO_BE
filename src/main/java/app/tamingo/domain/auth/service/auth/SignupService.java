package app.tamingo.domain.auth.service.auth;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.auth.exception.AuthErrorCode;
import app.tamingo.domain.auth.service.email.EmailVerificationService;
import app.tamingo.domain.terms.exception.TermsErrorCode;
import app.tamingo.common.security.JwtTokenProvider;
import app.tamingo.domain.auth.entity.AuthIdentity;
import app.tamingo.domain.auth.entity.AuthProvider;
import app.tamingo.domain.auth.redis.RefreshToken;
import app.tamingo.domain.auth.redis.RefreshTokenRepository;
import app.tamingo.domain.auth.redis.SignupSession;
import app.tamingo.domain.auth.redis.SignupSessionRepository;
import app.tamingo.domain.auth.repository.AuthIdentityRepository;
import app.tamingo.domain.terms.entity.Terms;
import app.tamingo.domain.terms.entity.TermsCode;
import app.tamingo.domain.terms.repository.TermsRepository;
import app.tamingo.domain.terms.repository.UserTermsAgreementRepository;
import app.tamingo.domain.user.entity.UserTermsAgreement;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class SignupService {

    private static final long SIGNUP_TTL_SEC = 900L; // 15분
    private static final int NICKNAME_MAX_LEN = 10;

    private static final Pattern PASSWORD_POLICY =
            Pattern.compile("^[\\x21-\\x7E]{8,16}$");

    private final SignupSessionRepository signupSessionRepository;
    private final TermsRepository termsRepository;
    private final UserRepository userRepository;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private final EmailVerificationService emailVerificationService;

    // 1. 회원가입 세션 생성 (약관 동의 포함)
    public String createSignupSession(Map<TermsCode, Boolean> agreedTerms) {

        Map<TermsCode, Boolean> safeMap = toSafeEnumMap(agreedTerms);

        // 필수 약관 동의 검증
        List<Terms> requiredTerms = termsRepository.findAllByIsRequiredTrue();
        for (Terms t : requiredTerms) {
            if (!Boolean.TRUE.equals(safeMap.get(t.getCode()))) {
                throw new CustomException(TermsErrorCode.REQUIRED_TERMS_NOT_AGREED);
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
            throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
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
            throw new CustomException(AuthErrorCode.SIGNUP_EMAIL_NOT_MATCHED);
        }

        // 인증번호 확인 실패 시
        boolean ok = emailVerificationService.verifyCode(email, code);
        if (!ok) {
            throw new CustomException(AuthErrorCode.SIGNUP_EMAIL_CODE_INVALID);
        }

        session.markEmailVerified();
        signupSessionRepository.save(session);
    }

    // 4. 아이디 생성 (회원가입 완료)
    public SignupResult completeSignup(String signupSessionId, String nickname, String password) {
        SignupSession session = getSessionOrThrow(signupSessionId);

        if (!session.isEmailVerified()) {
            throw new CustomException(AuthErrorCode.SIGNUP_EMAIL_NOT_VERIFIED);
        }

        String email = session.getEmail();
        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        // 닉네임 검증
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(AuthErrorCode.SIGNUP_NICKNAME_REQUIRED);
        }

        if (nickname.length() > NICKNAME_MAX_LEN) {
            throw new CustomException(AuthErrorCode.SIGNUP_NICKNAME_TOO_LONG);
        }

        // 비밀번호 검증
        if (password == null || password.isBlank()) {
            throw new CustomException(AuthErrorCode.SIGNUP_PASSWORD_REQUIRED);
        }

        if (!PASSWORD_POLICY.matcher(password).matches()) {
            throw new CustomException(AuthErrorCode.SIGNUP_PASSWORD_POLICY_INVALID);
        }

        if (userRepository.existsByEmail(email)) {
            throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 유저 생성
        User user = User.of(email, nickname);
        userRepository.save(user);

        // LOCAL 인증 정보 저장
        String hash = passwordEncoder.encode(password);
        authIdentityRepository.save(AuthIdentity.createLocal(user, email, hash));

        // 약관 동의 여부 저장
        List<Terms> currentTerms = termsRepository.findAll();
        Map<TermsCode, Boolean> agreedMap = toSafeEnumMap(session.getAgreedTerms());

        List<UserTermsAgreement> agreements = currentTerms.stream()
                .map(t -> UserTermsAgreement.agree(user, t, Boolean.TRUE.equals(agreedMap.get(t.getCode()))))
                .toList();
        userTermsAgreementRepository.saveAll(agreements);

        // 세션 삭제 및 토큰 발급
        signupSessionRepository.deleteById(signupSessionId);

        String access = jwtTokenProvider.createAccessToken(user.getId());
        String refresh = jwtTokenProvider.createRefreshToken(user.getId());

        long refreshTtlSec = jwtTokenProvider.getRefreshExpMs() / 1000;
        refreshTokenRepository.save(RefreshToken.create(user.getId(), refresh, refreshTtlSec));

        return new SignupResult(user.getId(), access, refresh);
    }

    public record SignupResult(
            Long userId,
            String accessToken,
            String refreshToken
    ) {}

    private SignupSession getSessionOrThrow(String signupSessionId) {
        if (signupSessionId == null || signupSessionId.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return signupSessionRepository.findById(signupSessionId)
                .orElseThrow(() -> new CustomException(AuthErrorCode.SIGNUP_SESSION_NOT_FOUND));
    }

    private Map<TermsCode, Boolean> toSafeEnumMap(Map<TermsCode, Boolean> source) {
        Map<TermsCode, Boolean> safe = new EnumMap<>(TermsCode.class);
        if (source != null) safe.putAll(source);
        return safe;
    }
}
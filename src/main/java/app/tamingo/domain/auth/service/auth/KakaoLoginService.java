package app.tamingo.domain.auth.service.auth;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.auth.dto.login.LoginResponse;
import app.tamingo.domain.auth.entity.AuthIdentity;
import app.tamingo.domain.auth.entity.AuthProvider;
import app.tamingo.domain.auth.exception.AuthErrorCode;
import app.tamingo.domain.auth.kakao.client.KakaoLoginClient;
import app.tamingo.domain.auth.kakao.dto.KakaoUserResponse;
import app.tamingo.domain.auth.redis.RefreshToken;
import app.tamingo.domain.auth.redis.RefreshTokenRepository;
import app.tamingo.domain.auth.repository.AuthIdentityRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.entity.UserStatus;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class KakaoLoginService {

    private final KakaoLoginClient kakaoLoginClient;
    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponse login(String kakaoAccessToken) {

        KakaoUserResponse me = kakaoLoginClient.getMe(kakaoAccessToken);

        String providerUserId = String.valueOf(me.id());
        KakaoUserResponse.KakaoAccount account = me.kakaoAccount();

        String email = (account != null) ? account.email() : null;
        if (email == null || email.isBlank()) {
            throw new CustomException(AuthErrorCode.KAKAO_EMAIL_REQUIRED);
        }

        String nickname =
                (account != null && account.profile() != null
                        && account.profile().nickname() != null
                        && !account.profile().nickname().isBlank())
                        ? account.profile().nickname()
                        : "kakao_" + providerUserId; // 닉네임이 없을 경우

        // 이미 카카오로 연결된 계정이면 그대로 로그인
        AuthIdentity identity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.KAKAO, providerUserId)
                .orElseGet(() -> linkOrCreateByEmail(email, nickname, providerUserId));

        User user = identity.getUser();

        if (user.getStatus() == UserStatus.DELETED) {
            throw new CustomException(UserErrorCode.USER_DELETED);
        }

        Long userId = user.getId();

        // 토큰 발급
        String access = jwtTokenProvider.createAccessToken(userId);
        String refresh = jwtTokenProvider.createRefreshToken(userId);

        long refreshTtlSec = jwtTokenProvider.getRefreshExpMs() / 1000;
        refreshTokenRepository.save(RefreshToken.create(userId, refresh, refreshTtlSec));

        return new LoginResponse(
                userId,
                access,
                refresh,
                user.isOnboardingCompleted()
        );
    }

    /**
     * 카카오 최초 로그인인데 providerUserId 연결이 없을 때
     * - 같은 email의 기존 User가 있으면 -> 그 User에 KAKAO AuthIdentity만 추가
     * - 없으면 -> User 생성 후 AuthIdentity 생성
     */
    private AuthIdentity linkOrCreateByEmail(String email, String nickname, String providerUserId) {

        return userRepository.findByEmail(email)
                .map(user -> {
                    // 같은 이메일로 이미 KAKAO가 연결돼 있으면 막기
                    if (authIdentityRepository.existsByProviderAndEmail(AuthProvider.KAKAO, email)) {
                        throw new CustomException(AuthErrorCode.KAKAO_ALREADY_LINKED);
                    }
                    return authIdentityRepository.save(
                            AuthIdentity.createKakao(user, providerUserId, email)
                    );
                })
                .orElseGet(() -> {
                    User user = userRepository.save(User.of(email, nickname));
                    return authIdentityRepository.save(
                            AuthIdentity.createKakao(user, providerUserId, email)
                    );
                });
    }
}
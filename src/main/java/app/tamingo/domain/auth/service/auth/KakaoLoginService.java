package app.tamingo.domain.auth.service.auth;

import app.tamingo.common.security.JwtTokenProvider;
import app.tamingo.domain.auth.dto.login.LoginResponse;
import app.tamingo.domain.auth.entity.AuthIdentity;
import app.tamingo.domain.auth.entity.AuthProvider;
import app.tamingo.domain.auth.kakao.client.KakaoLoginClient;
import app.tamingo.domain.auth.kakao.dto.KakaoUserResponse;
import app.tamingo.domain.auth.repository.AuthIdentityRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
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

    public LoginResponse login(String kakaoAccessToken) {
        KakaoUserResponse me = kakaoLoginClient.getMe(kakaoAccessToken);

        String providerUserId = String.valueOf(me.id());
        KakaoUserResponse.KakaoAccount account = me.kakaoAccount();

        final String finalEmail =
                (account != null) ? account.email() : null;

        final String finalNickname =
                (account != null && account.profile() != null
                        && account.profile().nickname() != null
                        && !account.profile().nickname().isBlank())
                        ? account.profile().nickname()
                        : "kakao_" + providerUserId; // 닉네임 없는 경우 대비

        AuthIdentity identity = authIdentityRepository
                .findByProviderAndProviderUserId(AuthProvider.KAKAO, providerUserId)
                .orElseGet(() -> {
                    User user = userRepository.save(User.of(finalEmail, finalNickname));
                    return authIdentityRepository.save(
                            AuthIdentity.createKakao(user, providerUserId, finalEmail)
                    );
                });

        User user = identity.getUser();

        return new LoginResponse(
                user.getId(),
                jwtTokenProvider.createAccessToken(user.getId()),
                jwtTokenProvider.createRefreshToken(user.getId()),
                user.isOnboardingCompleted()
        );
    }
}
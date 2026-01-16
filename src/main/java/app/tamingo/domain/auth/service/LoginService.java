package app.tamingo.domain.auth.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.common.security.JwtTokenProvider;
import app.tamingo.domain.auth.entity.AuthIdentity;
import app.tamingo.domain.auth.entity.AuthProvider;
import app.tamingo.domain.auth.redis.RefreshToken;
import app.tamingo.domain.auth.redis.RefreshTokenRepository;
import app.tamingo.domain.auth.repository.AuthIdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {

    private final AuthIdentityRepository authIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResult login(String email, String password) {
        AuthIdentity ai = authIdentityRepository
                .findByProviderAndEmail(AuthProvider.LOCAL, email)
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_EMAIL_NOT_FOUND));

        if (ai.getPasswordHash() == null || ai.getPasswordHash().isBlank()) {
            throw new CustomException(ErrorCode.LOGIN_PASSWORD_NOT_SET);
        }

        if (!passwordEncoder.matches(password, ai.getPasswordHash())) {
            throw new CustomException(ErrorCode.LOGIN_PASSWORD_INVALID);
        }

        Long userId = ai.getUser().getId();

        String access = jwtTokenProvider.createAccessToken(userId);
        String refresh = jwtTokenProvider.createRefreshToken(userId);

        long refreshTtlSec = jwtTokenProvider.getRefreshExpMs() / 1000;
        refreshTokenRepository.save(RefreshToken.create(userId, refresh, refreshTtlSec));

        return new LoginResult(userId, access, refresh);
    }

    public record LoginResult(Long userId, String accessToken, String refreshToken) {}
}
package app.tamingo.domain.auth.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.error.AuthErrorCode;
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

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final AuthIdentityRepository authIdentityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResult login(String email, String password) {
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new CustomException(AuthErrorCode.LOGIN_EMAIL_FORMAT_INVALID);
        }

        AuthIdentity ai = authIdentityRepository
                .findByProviderAndEmail(AuthProvider.LOCAL, email)
                .orElseThrow(() -> new CustomException(AuthErrorCode.LOGIN_EMAIL_NOT_FOUND));

        if (ai.getPasswordHash() == null || ai.getPasswordHash().isBlank()) {
            throw new CustomException(AuthErrorCode.LOGIN_PASSWORD_NOT_SET);
        }

        if (!passwordEncoder.matches(password, ai.getPasswordHash())) {
            throw new CustomException(AuthErrorCode.LOGIN_PASSWORD_INVALID);
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
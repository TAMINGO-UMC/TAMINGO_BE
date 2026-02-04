package app.tamingo.domain.auth.service.auth;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.security.JwtTokenProvider;
import app.tamingo.domain.auth.dto.token.TokenRefreshResponse;
import app.tamingo.domain.auth.exception.AuthErrorCode;
import app.tamingo.domain.auth.redis.RefreshToken;
import app.tamingo.domain.auth.redis.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenRefreshService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRefreshResponse refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.TOKEN_MISSING);
        }

        // refresh token 자체 유효성 검증
        jwtTokenProvider.validateOrThrow(refreshToken);

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        String key = "refresh:" + userId;

        RefreshToken saved = refreshTokenRepository.findById(key)
                .orElseThrow(() -> new CustomException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!refreshToken.equals(saved.getToken())) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 새 access token 발급
        String newAccess = jwtTokenProvider.createAccessToken(userId);

        return new TokenRefreshResponse(newAccess);
    }
}
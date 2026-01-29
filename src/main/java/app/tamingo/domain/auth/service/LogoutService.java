package app.tamingo.domain.auth.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.auth.exception.AuthErrorCode;
import app.tamingo.common.security.JwtTokenProvider;
import app.tamingo.domain.auth.redis.RefreshToken;
import app.tamingo.domain.auth.redis.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LogoutService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public void logout(String accessToken, String refreshToken) {

        if (accessToken == null || accessToken.isBlank()) {
            throw new CustomException(AuthErrorCode.TOKEN_MISSING);
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.TOKEN_MISSING);
        }

        validateOrThrow(accessToken);
        validateOrThrow(refreshToken);

        Long userIdFromAccess = jwtTokenProvider.getUserId(accessToken);
        Long userIdFromRefresh = jwtTokenProvider.getUserId(refreshToken);

        if (!userIdFromAccess.equals(userIdFromRefresh)) {
            throw new CustomException(AuthErrorCode.TOKEN_INVALID);
        }

        // Redis refresh 존재/일치 확인
        String key = "refresh:" + userIdFromAccess;

        RefreshToken saved = refreshTokenRepository.findById(key)
                .orElseThrow(() -> new CustomException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!refreshToken.equals(saved.getToken())) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // Refresh Token 삭제 (로그아웃)
        refreshTokenRepository.deleteById(key);
    }

    private void validateOrThrow(String token) {
        try {
            jwtTokenProvider.getUserId(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(AuthErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(AuthErrorCode.TOKEN_INVALID);
        }
    }
}
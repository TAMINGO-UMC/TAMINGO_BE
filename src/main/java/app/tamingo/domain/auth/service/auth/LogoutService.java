package app.tamingo.domain.auth.service.auth;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.auth.exception.AuthErrorCode;
import app.tamingo.common.security.JwtTokenProvider;
import app.tamingo.domain.auth.redis.RefreshToken;
import app.tamingo.domain.auth.redis.RefreshTokenRepository;
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

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(AuthErrorCode.TOKEN_MISSING);
        }

        jwtTokenProvider.validateOrThrow(refreshToken);

        Long userIdFromRefresh = jwtTokenProvider.getUserId(refreshToken);

        // access token이 있다면 userId 일치 확인
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                Long userIdFromAccess = jwtTokenProvider.getUserId(accessToken);
                if (!userIdFromAccess.equals(userIdFromRefresh)) {
                    throw new CustomException(AuthErrorCode.TOKEN_INVALID);
                }
            } catch (CustomException e) {
                // refresh 기준 로그아웃이라 access token 에러 무시
            }
        }

        // Redis refresh 존재/일치 확인
        String key = "refresh:" + userIdFromRefresh;

        RefreshToken saved = refreshTokenRepository.findById(key)
                .orElseThrow(() -> new CustomException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!refreshToken.equals(saved.getToken())) {
            throw new CustomException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // Refresh Token 삭제 (로그아웃)
        refreshTokenRepository.deleteById(key);
    }
}
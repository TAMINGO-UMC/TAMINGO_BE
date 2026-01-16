package app.tamingo.common.security;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-ms}") long accessExpMs,
            @Value("${jwt.refresh-exp-ms}") long refreshExpMs
    ) {
        // HS256용 키 생성
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessExpMs);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshExpMs);
    }

    public long getRefreshExpMs() {
        return refreshExpMs;
    }

    private String createToken(Long userId, long expMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key) // 0.12.x OK
                .compact();
    }

    public Long getUserId(String token) {
        Claims claims = parse(token);
        return Long.valueOf(claims.getSubject());
    }

    public void validateOrThrow(String token) {
        try {
            parse(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
        } catch (SecurityException | SignatureException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
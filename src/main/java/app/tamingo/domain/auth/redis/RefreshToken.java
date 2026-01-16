package app.tamingo.domain.auth.redis;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@NoArgsConstructor
@RedisHash("refresh_token")
public class RefreshToken {

    @Id
    private String key; // "refresh:{userId}"

    private String token;

    @TimeToLive
    private Long ttlSec;

    public static RefreshToken create(Long userId, String token, long ttlSec) {
        RefreshToken rt = new RefreshToken();
        rt.key = "refresh:" + userId;
        rt.token = token;
        rt.ttlSec = ttlSec;
        return rt;
    }
}
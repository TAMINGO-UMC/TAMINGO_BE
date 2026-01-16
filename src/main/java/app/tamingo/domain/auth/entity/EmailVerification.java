package app.tamingo.domain.auth.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash("email:verify")
public class EmailVerification {

    @Id
    private String email;

    private String code;

    @TimeToLive
    private Long ttl;

    public static EmailVerification create(String email, String code, long ttl) {
        EmailVerification ev = new EmailVerification();
        ev.email = email;
        ev.code = code;
        ev.ttl = ttl;
        return ev;
    }
}
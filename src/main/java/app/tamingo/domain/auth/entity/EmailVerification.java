package app.tamingo.domain.auth.entity;

import lombok.Builder;
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

    @Builder(builderMethodName = "internalBuilder")
    private EmailVerification(String email, String code, Long ttl) {
        this.email = email;
        this.code = code;
        this.ttl = ttl;
    }

    public static EmailVerification of(String email, String code, long ttl) {
        return EmailVerification.internalBuilder()
                .email(email)
                .code(code)
                .ttl(ttl)
                .build();
    }
}
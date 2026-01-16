package app.tamingo.domain.auth.redis;

import app.tamingo.domain.terms.entity.TermsCode;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.EnumMap;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("signup:session")
public class SignupSession {

    @Id
    private String id; // signupSessionId (uuid)

    private Map<TermsCode, Boolean> agreedTerms = new EnumMap<>(TermsCode.class);

    private String email;
    private boolean emailVerified;

    private SignupStep step;

    @TimeToLive
    private Long ttlSec;

    public static SignupSession create(String id, Map<TermsCode, Boolean> agreedTerms, long ttlSec) {
        SignupSession s = new SignupSession();
        s.id = id;

        s.agreedTerms = new EnumMap<>(TermsCode.class);
        if (agreedTerms != null) {
            s.agreedTerms.putAll(agreedTerms);
        }

        s.emailVerified = false;
        s.step = SignupStep.TERMS_AGREED;
        s.ttlSec = ttlSec;
        return s;
    }

    public void setEmail(String email) {
        this.email = email;
        if (this.step != SignupStep.EMAIL_VERIFIED) {
            this.step = SignupStep.EMAIL_SENT;
        }
    }

    public void markEmailVerified() {
        this.emailVerified = true;
        this.step = SignupStep.EMAIL_VERIFIED;
    }
}
package app.tamingo.domain.auth.exception;

import app.tamingo.common.exception.DataIntegrityMapper;
import app.tamingo.common.response.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class KakaoEmailDupMapper implements DataIntegrityMapper {

    private static final String CONSTRAINT =
            "uq_auth_identities_provider_email";

    @Override
    public boolean supports(String key) {
        return CONSTRAINT.equals(key);
    }

    @Override
    public BaseCode errorCode() {
        // 이메일로 이미 카카오가 연결됨
        return AuthErrorCode.KAKAO_ALREADY_LINKED;
    }
}
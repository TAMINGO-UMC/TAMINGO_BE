package app.tamingo.domain.auth.exception;

import app.tamingo.common.exception.DataIntegrityMapper;
import app.tamingo.common.response.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class KakaoProviderUserIdDupMapper implements DataIntegrityMapper {

    private static final String CONSTRAINT =
            "uq_auth_identities_provider_provider_user_id";

    @Override
    public boolean supports(String key) {
        return CONSTRAINT.equals(key);
    }

    @Override
    public BaseCode errorCode() {
        // 같은 카카오 계정이 이미 연결됨
        return AuthErrorCode.KAKAO_ACCOUNT_ALREADY_USED;
    }
}
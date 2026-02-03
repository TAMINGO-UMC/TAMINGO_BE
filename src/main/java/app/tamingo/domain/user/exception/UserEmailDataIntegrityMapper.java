package app.tamingo.domain.user.exception;

import app.tamingo.common.exception.DataIntegrityMapper;
import app.tamingo.common.response.BaseCode;
import app.tamingo.domain.auth.exception.AuthErrorCode;
import org.springframework.stereotype.Component;

@Component
public class UserEmailDataIntegrityMapper implements DataIntegrityMapper {

    @Override
    public boolean supports(String key) {
        // User.email 유니크 제약 이름
        return "uq_users_email".equals(key);
    }

    @Override
    public BaseCode errorCode() {
        return AuthErrorCode.EMAIL_ALREADY_EXISTS;
    }
}
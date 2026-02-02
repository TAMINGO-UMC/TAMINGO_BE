package app.tamingo.domain.user.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.auth.redis.RefreshTokenRepository;
import app.tamingo.domain.auth.repository.AuthIdentityRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.entity.UserStatus;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        String refreshKey = "refresh:" + userId;

        // 이미 탈퇴한 경우 멱등 처리
        if (user.getStatus() == UserStatus.DELETED) {
            refreshTokenRepository.deleteById(refreshKey);
            return;
        }

        // 재가입 허용
        authIdentityRepository.deleteAllByUserId(userId);
        refreshTokenRepository.deleteById(refreshKey);

        user.withdraw();
    }
}
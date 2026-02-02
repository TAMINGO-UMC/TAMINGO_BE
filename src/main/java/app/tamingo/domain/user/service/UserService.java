package app.tamingo.domain.user.service;

import app.tamingo.common.exception.CustomException;
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

    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.DELETED) {
            return; // 이미 탈퇴한 경우 멱등 처리
        }

        user.withdraw();
    }
}
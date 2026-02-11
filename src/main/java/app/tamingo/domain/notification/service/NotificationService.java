package app.tamingo.domain.notification.service;

import app.tamingo.domain.notification.entity.DeviceToken;
import app.tamingo.domain.notification.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final DeviceTokenRepository deviceTokenRepository;

    public void registerDeviceToken(Long userId, String token) {
        if(!deviceTokenRepository.existsByUserIdAndToken(userId, token)) {
            log.info("[토큰 등록 실패] 이미 존재하는 토큰입니다. userId:{}", userId);
            return;
        }

        DeviceToken deviceToken = DeviceToken.of(userId, token);
        deviceTokenRepository.save(deviceToken);

        log.info("[토큰 등록 성공] userId:{}, token:{}", userId, token);
    }
}

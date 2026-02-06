package app.tamingo.domain.notification.service;

import app.tamingo.domain.notification.repository.DeviceTokenRepository; // 레포지토리 주입
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Async
    @Transactional
    public void sendNotification(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            FirebaseMessaging.getInstance().send(message);
            log.info("[FCM 발송 성공] Token: {}", token);

        } catch (FirebaseMessagingException e) {
            // 토큰이 만료되었거나 유효하지 않은 경우
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                    e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {

                log.warn("[만료된 토큰] : {}", token);

                // DB에서 해당 토큰을 찾아 isActive = false로 변경
                deviceTokenRepository.findByToken(token).ifPresent(deviceToken -> {
                    deviceToken.deactivate(); // isActive = false로 변경
                    log.info("[만료된 토큰 비활성화] : {}", deviceToken.getId());
                });
            } else {
                log.error("[FCM 발송 중 기타 에러 발생] : {}", e.getMessage());
            }
        }
    }
}
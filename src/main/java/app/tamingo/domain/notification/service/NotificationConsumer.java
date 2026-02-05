package app.tamingo.domain.notification.service;

import app.tamingo.domain.notification.dto.NotificationMessage;
import app.tamingo.domain.notification.entity.DeviceToken;
import app.tamingo.domain.notification.repository.DeviceTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 변환기 주입
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmService fcmService;
    private final ObjectMapper objectMapper;

    public void sendPushFromJson(String jsonMessage) {
        try {
            // JSON 글자를 NotificationMessage 객체로 변환
            NotificationMessage msg = objectMapper.readValue(jsonMessage, NotificationMessage.class);

            sendPush(msg);

        } catch (Exception e) {
            log.error("[JSON 파싱 실패 혹은 발송 오류] : {}", jsonMessage, e);
            // 스케줄러가 Redis에서 삭제하지 않고 재시도
            throw new RuntimeException("알림 처리 실패", e);
        }
    }

    public void sendPush(NotificationMessage msg) {
        // 활성화된 모든 토큰 가져오기
        List<DeviceToken> activeTokens = deviceTokenRepository.findAllByUserIdAndIsActiveTrue(msg.getUserId());

        if (activeTokens.isEmpty()) {
            log.warn("[활성 토큰 오류] 유저 {}의 활성 토큰 미존재.", msg.getUserId());
            return;
        }

        // 메시지 본문 생성
        String body = createBody(msg);

        // 각 기기로 발송
        for (DeviceToken deviceToken : activeTokens) {
            fcmService.sendNotification(deviceToken.getToken(), "Tamingo", body);
        }
    }

    private String createBody(NotificationMessage msg) {
        // 소요 시간 변환
        int eta = msg.getExpectedEta();
        String timeText;

        if (eta >= 60) {
            int hours = eta / 60;
            int minutes = eta % 60;
            timeText = (minutes == 0) ? String.format("%d시간", hours) : String.format("%d시간 %d분", hours, minutes);
        } else {
            timeText = String.format("%d분", eta);
        }

        return switch (msg.getType()) {
            case BEFORE_20MIN -> String.format("%s님, 출발까지 20분 남았습니다. 지금 교통상황을 보니 [%s]까지 약 %s 걸릴 것 같아요. 슬슬 준비해 볼까요?",
                    msg.getUserName(), msg.getDestination(), timeText);

            case GENERAL -> String.format("%s님, 이제 [%s]로 지금 출발할 시간이에요! 지금 나가야 정시에 도착할 수 있습니다.",
                    msg.getUserName(), msg.getDestination());

            case PERSONAL -> String.format("%s님의 평소 보폭을 반영해 조금 일찍 알려드려요. 지금 출발하면 [%s]에 여유있게 도착합니다!",
                    msg.getUserName(), msg.getDestination());

            case TRAFFIC -> String.format("가는 길 교통이 혼잡해요! 평소보다 10분만 서둘러 출발해 볼까요?");

            case ARRIVAL_CHECK -> String.format("%s님, [%s]에 잘 도착하셨나요? 도착하셨다면 버튼을 눌러 상태를 변경해 주세요!",
                    msg.getUserName(), msg.getDestination());

            default -> "Tamingo 알림이 도착했습니다.";
        };
    }
}
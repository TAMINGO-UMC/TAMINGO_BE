package app.tamingo.domain.notificationsetting.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.notificationsetting.dto.NotificationSettingRequest;
import app.tamingo.domain.notificationsetting.dto.NotificationSettingResponse;
import app.tamingo.domain.notificationsetting.exception.NotificationSettingException;
import app.tamingo.domain.notificationsetting.entity.AlertMinute;
import app.tamingo.domain.notificationsetting.entity.NotificationSetting;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class NotificationSettingService {

    private final UserRepository userRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    // 업데이트
    public NotificationSettingResponse update(Long userId, NotificationSettingRequest.UpdateDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        NotificationSetting setting = notificationSettingRepository.findById(userId)
                .orElseThrow(() -> new CustomException(NotificationSettingException.NOTIFICATION_SETTING_NOT_FOUND));

        setting.update(
                dto.departureAlertEnabled(),
                dto.departureLeadMinutes(),
                dto.realtimeTransitEnabled(),
                dto.todoProposalEnabled(),
                dto.locationMoveCheckEnabled(),
                dto.routineAlertEnabled()
        );

        return NotificationSettingResponse.from(setting);

    }

    // 업데이트
    public NotificationSettingResponse getSetting(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        NotificationSetting setting = notificationSettingRepository.findById(userId)
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.of(user)));

        return NotificationSettingResponse.from(setting);
    }

    // 온보딩 페이지 알림 설정
    public void applyOnboarding(Long userId, boolean enabled, AlertMinute minute) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        NotificationSetting setting = notificationSettingRepository.findById(userId)
                .orElseGet(() -> notificationSettingRepository.save(NotificationSetting.of(user)));

        int leadMinutes = minute.getMinutes();

        setting.updateDepartureAlert(enabled, leadMinutes);
    }
}

package app.tamingo.domain.calendar.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.calendar.dto.AppleCalendarIntegrationStatusResponse;
import app.tamingo.domain.calendar.dto.AppleCalendarIntegrationToggleRequest;
import app.tamingo.domain.calendar.entity.CalendarIntegration;
import app.tamingo.domain.calendar.enums.CalendarIntegrationStatus;
import app.tamingo.domain.calendar.repository.CalendarIntegrationRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Apple 캘린더 연동 상태 조회/토글 서비스
@Service
@RequiredArgsConstructor
public class AppleCalendarIntegrationService {

    private final CalendarIntegrationRepository calendarIntegrationRepository;
    private final UserRepository userRepository;

    //연동 상태 조회
    @Transactional(readOnly = true)
    public AppleCalendarIntegrationStatusResponse getStatus(Long userId) {

        //유저 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 연동이 없으면 "비활성 상태"로 보여주기(생성은 토글 ON 시점에 해도 됨)
        return calendarIntegrationRepository.findByUserId(userId)
                .map(integration -> new AppleCalendarIntegrationStatusResponse(
                        integration.getStatus() == CalendarIntegrationStatus.ACTIVE,
                        integration.getStatus(),
                        integration.getLastSyncedAt()
                ))
                .orElseGet(() -> new AppleCalendarIntegrationStatusResponse(
                        false,
                        CalendarIntegrationStatus.INACTIVE,
                        null
                ));
    }

    //연동 토글(PATCH)
    @Transactional
    public AppleCalendarIntegrationStatusResponse toggle(Long userId, AppleCalendarIntegrationToggleRequest request) {

        //유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 연동 엔티티 없으면 생성(토글 시점에 생성하는 정책)
        CalendarIntegration integration = calendarIntegrationRepository.findByUserId(userId)
                .orElseGet(() -> calendarIntegrationRepository.save(CalendarIntegration.of(user)));


        if (Boolean.TRUE.equals(request.enabled())) {
            integration.updateSyncFromApple(true);
            integration.markActive();
        } else {
            integration.updateSyncFromApple(false);
            integration.markInactive();
        }


        return new AppleCalendarIntegrationStatusResponse(
                integration.getStatus() == CalendarIntegrationStatus.ACTIVE,
                integration.getStatus(),
                integration.getLastSyncedAt()
        );
    }
}

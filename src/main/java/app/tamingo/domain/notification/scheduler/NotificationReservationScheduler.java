package app.tamingo.domain.notification.scheduler;

import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.home.repository.ScheduleStartSnapshotRepository;
import app.tamingo.domain.notification.dto.NotificationMessage;
import app.tamingo.domain.notification.enums.NotificationType;
import app.tamingo.domain.notification.service.NotificationProducer;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.DepartureAlarm;
import app.tamingo.domain.userlearning.repository.DepartureAlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationReservationScheduler {

    private final NotificationProducer notificationProducer;
    private final ScheduleStartSnapshotRepository snapshotRepository;
    private final DepartureAlarmRepository departureAlarmRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void reserveNotifications() {
        List<ScheduleStartSnapshot> newSnapshots = snapshotRepository.findAllByIsReservedFalse();

        for (ScheduleStartSnapshot snapshot : newSnapshots) {
            try {
                User user = snapshot.getSchedule().getUser();
                String destination = snapshot.getSchedule().getPlaceName();
                int eta = snapshot.getExpectedEta();
                LocalDateTime startTime = snapshot.getSchedule().getStartTime();
                LocalDateTime departureTime = startTime.minusMinutes(eta);
                Long scheduleId = snapshot.getSchedule().getId();

                // --- [1번 알림: 출발 20분 전 사전 알림 예약] ---
                notificationProducer.reserve(
                        NotificationMessage.createDepartureBefore(user.getId(), user.getNickname(), destination, eta),
                        departureTime.minusMinutes(20)
                );

                // --- [2번/3번 알림: 정각 알림 결정 로직] ---
                NotificationMessage onTimeMessage;

                Optional<DepartureAlarm> alarmOpt = departureAlarmRepository.findByUserId(user.getId());

                if (alarmOpt.isPresent()) {
                    DepartureAlarm alarm = alarmOpt.get();

                    // USF 데이터가 있고 아직 적용 안내를 받지 않은 경우 -> [3번] 개인화 알림
                    if (!alarm.isApplied()) {
                        onTimeMessage = NotificationMessage.createCustom(user.getId(), user.getNickname(), destination, eta);

                        alarm.markAsApplied(); // 고지 완료 상태로 변경
                        log.info("[3번(개인화) 예약] {}님 새로운 예약", user.getNickname());
                    }
                    // 이미 고지된 경우 -> [2번] 일반 알림
                    else {
                        onTimeMessage = NotificationMessage.createGeneral(user.getId(), user.getNickname(), destination, eta);
                    }
                }
                // USF 데이터가 없는 경우 -> [2번] 일반 알림
                else {
                    onTimeMessage = NotificationMessage.createGeneral(user.getId(), user.getNickname(), destination, eta);
                }

                // 결정된 정각 알림(2번 또는 3번) 예약
                notificationProducer.reserve(onTimeMessage, departureTime);

                // [10번 알림 : N분 전]
                notificationSettingRepository.findById(user.getId()).ifPresent(setting -> {
                    if (setting.isDepartureAlertEnabled()) {
                        int leadMinutes = setting.getDepartureLeadMinutes();
                        LocalDateTime reminderTime = departureTime.minusMinutes(leadMinutes);

                        notificationProducer.reserve(
                                NotificationMessage.createNMinutes(user.getId(), user.getNickname(), destination, leadMinutes),
                                reminderTime
                        );
                        log.info("[10번(n분전) 예약] {}님 {}분 전 알림", user.getNickname(), leadMinutes);
                    }
                });

                // [7번 알림 : Silent GPS 체크 예약]
                LocalDateTime silentGpsTime = startTime.minusHours(1);

                notificationProducer.reserve(
                        NotificationMessage.createSilentGps(user.getId(), user.getNickname(), scheduleId),
                        silentGpsTime
                );

                log.info("[7번 Silent GPS 예약] {} 님 Silent GPS 예약", user.getNickname());

                snapshot.reserved();
                log.info("{}님 알림세트 예약 성공", user.getNickname());

            } catch (Exception e) {
                log.error("알림 예약 실패 -> snapshotId={}", snapshot.getId(), e);
            }
        }
    }
}
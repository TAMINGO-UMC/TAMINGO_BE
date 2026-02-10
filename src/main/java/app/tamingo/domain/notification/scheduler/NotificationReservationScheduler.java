package app.tamingo.domain.notification.scheduler;
import app.tamingo.domain.home.entity.ScheduleStartSnapshot;
import app.tamingo.domain.home.entity.enums.SuggestionType;
import app.tamingo.domain.home.repository.ScheduleStartSnapshotRepository;
import app.tamingo.domain.home.repository.SuggestionLearningRepository;
import app.tamingo.domain.notification.dto.NotificationMessage;
import app.tamingo.domain.notification.service.NotificationProducer;
import app.tamingo.domain.notificationsetting.repository.NotificationSettingRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.useractivetime.service.UserActiveTimeService;
import app.tamingo.domain.userlearning.entity.DepartureAlarm;
import app.tamingo.domain.userlearning.repository.DepartureAlarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
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
    private final UserActiveTimeService userActiveTimeService;
    private final SuggestionLearningRepository suggestionRepository;


    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void reserveNotifications() {
        if (isQuietHours()) {
            return;
        }
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

                // [7번 알림 : Silent GPS 체크 예약]
                LocalDateTime silentGpsTime = startTime.minusHours(1);

                notificationProducer.reserve(
                        NotificationMessage.createSilentGps(user.getId(), user.getNickname(), scheduleId),
                        silentGpsTime
                );

                log.info("[7번 Silent GPS 예약] {} 님 Silent GPS 예약", user.getNickname());

                // [11번 알림]
                suggestionRepository.findBestRouteSuggestion(user, SuggestionType.ROUTE_DETOUR, LocalDate.now().atStartOfDay())
                        .ifPresent(suggestion -> {

                            LocalDateTime alertTime = departureTime.minusMinutes(20);

                            if (alertTime.isAfter(LocalDateTime.now())) {
                                notificationProducer.reserve(
                                        NotificationMessage.createRouteLink(
                                                user.getId(),
                                                user.getNickname(),
                                                suggestion.getPlaceName(),
                                                suggestion.getTitle()
                                        ),
                                        alertTime
                                );
                                log.info("[11번 예약] {}님 {}분 일찍 출발 제안", user.getNickname(), eta + 20);
                            }
                        });
                snapshot.reserved();
                log.info("{}님 알림세트 예약 성공", user.getNickname());

            } catch (Exception e) {
                log.error("알림 예약 실패 -> snapshotId={}", snapshot.getId(), e);
            }
        }
    }

    private boolean isQuietHours() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        return !now.isBefore(LocalTime.of(1, 0)) && now.isBefore(LocalTime.of(8, 0));
    }

    public void reserveGapNotification(User user, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        suggestionRepository.findBestGapForNotification(user, SuggestionType.GAP_TIME, startOfDay)
                .ifPresent(suggestion -> {

                    var activeResponse = userActiveTimeService.getUserActiveTime(user.getId());
                    LocalDateTime alertTime = LocalDateTime.of(date, activeResponse.startTime());

                    notificationProducer.reserve(
                            NotificationMessage.createGapTime(
                                    user.getId(),
                                    user.getNickname(),
                                    suggestion.getTitle(),
                                    suggestion.getDuration()
                            ),
                            alertTime
                    );

                    log.info("[8번 틈새 시간 예약] {}님 - {}", user.getNickname(), suggestion.getTitle());
                });
    }

}

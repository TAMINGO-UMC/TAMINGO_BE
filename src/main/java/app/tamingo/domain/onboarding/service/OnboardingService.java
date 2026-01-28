package app.tamingo.domain.onboarding.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.error.OnboardingErrorCode;
import app.tamingo.common.response.error.UserErrorCode;
import app.tamingo.domain.onboarding.dto.OnboardingRequest;
import app.tamingo.domain.onboarding.entity.*;
import app.tamingo.domain.onboarding.repository.*;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private static final int MAX_FAVORITE_PLACES = 5;

    private final UserRepository userRepository;

    private final UserActiveTimeSettingRepository activeTimeRepo;
    private final FavoritePlaceRepository favoritePlaceRepo;
    private final TransportPreferenceRepository transportPreferenceRepo;
    private final NotificationSettingRepository notificationSettingRepo;

    public void saveOnboarding(Long userId, OnboardingRequest req) {
        User user = getUserOrThrow(userId);

        ActiveTimeValue activeTime = validateAndParseActiveTime(req.activeTime());
        upsertActiveTime(user, activeTime, req.activeTime());

        validateFavoritePlaces(req.favoritePlaces());
        replaceFavoritePlaces(user, req.favoritePlaces());

        validateTransportPreferences(req.transportPreferences());
        replaceTransportPreferences(user, req.transportPreferences());

        AlertMinute minute = validateAndResolveAlertMinute(req.notificationSetting());
        upsertNotificationSetting(user, req.notificationSetting().departAlertEnabled(), minute);

        user.completeOnboarding();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }

    // 활동 시간
    private record ActiveTimeValue(LocalTime start, LocalTime end) {}

    private ActiveTimeValue validateAndParseActiveTime(OnboardingRequest.ActiveTime at) {
        LocalTime start = parseTimeOrThrow(at.startTime());
        LocalTime end = parseTimeOrThrow(at.endTime());

        if (!end.isAfter(start)) {
            throw new CustomException(OnboardingErrorCode.ONBOARDING_ACTIVE_TIME_RANGE_INVALID);
        }
        return new ActiveTimeValue(start, end);
    }

    private LocalTime parseTimeOrThrow(String s) {
        try {
            return LocalTime.parse(s); // expects "HH:mm"
        } catch (DateTimeParseException e) {
            throw new CustomException(OnboardingErrorCode.ONBOARDING_ACTIVE_TIME_FORMAT_INVALID);
        }
    }

    private void upsertActiveTime(User user, ActiveTimeValue v, OnboardingRequest.ActiveTime at) {
        Long userId = user.getId();

        activeTimeRepo.findById(userId).ifPresentOrElse(
                existing -> existing.update(
                        v.start(), v.end(),
                        at.monEnabled(),
                        at.tueEnabled(),
                        at.wedEnabled(),
                        at.thuEnabled(),
                        at.friEnabled(),
                        at.weekendEnabled()
                ),
                () -> activeTimeRepo.save(UserActiveTimeSetting.create(
                        user,
                        v.start(), v.end(),
                        at.monEnabled(),
                        at.tueEnabled(),
                        at.wedEnabled(),
                        at.thuEnabled(),
                        at.friEnabled(),
                        at.weekendEnabled()
                ))
        );
    }

    // 자주 가는 장소
    private void validateFavoritePlaces(List<OnboardingRequest.FavoritePlace> list) {
        if (list == null || list.isEmpty()) {
            throw new CustomException(OnboardingErrorCode.ONBOARDING_FAVORITE_PLACES_EMPTY);
        }
        if (list.size() > MAX_FAVORITE_PLACES) {
            throw new CustomException(OnboardingErrorCode.ONBOARDING_FAVORITE_PLACES_LIMIT_EXCEEDED);
        }
    }

    private void replaceFavoritePlaces(User user, List<OnboardingRequest.FavoritePlace> list) {
        Long userId = user.getId();

        favoritePlaceRepo.deleteAllByUserId(userId);
        for (OnboardingRequest.FavoritePlace p : list) {
            favoritePlaceRepo.save(FavoritePlace.create(
                    user,
                    p.name(),
                    p.address(),
                    p.latitude(),
                    p.longitude()
            ));
        }
    }

    // 선호 이동 수단
    private void validateTransportPreferences(List<OnboardingRequest.TransportPref> list) {
        if (list == null || list.size() != 3) {
            throw new CustomException(OnboardingErrorCode.ONBOARDING_TRANSPORT_PREFERENCES_INVALID);
        }

        Set<TransportType> types = new HashSet<>();
        Set<Integer> ranks = new HashSet<>();

        for (OnboardingRequest.TransportPref tp : list) {
            if (tp.transport() == null) {
                throw new CustomException(OnboardingErrorCode.ONBOARDING_TRANSPORT_PREFERENCES_INVALID);
            }
            types.add(tp.transport());
            ranks.add(tp.rank());
        }

        boolean hasAllTypes = types.size() == 3
                && types.contains(TransportType.BUS)
                && types.contains(TransportType.SUBWAY)
                && types.contains(TransportType.WALK);

        boolean hasAllRanks = ranks.size() == 3
                && ranks.contains(1)
                && ranks.contains(2)
                && ranks.contains(3);

        if (!hasAllTypes || !hasAllRanks) {
            throw new CustomException(OnboardingErrorCode.ONBOARDING_TRANSPORT_PREFERENCES_INVALID);
        }
    }

    private void replaceTransportPreferences(User user, List<OnboardingRequest.TransportPref> list) {
        Long userId = user.getId();

        transportPreferenceRepo.deleteAllByUserId(userId);
        for (OnboardingRequest.TransportPref tp : list) {
            transportPreferenceRepo.save(TransportPreference.create(
                    user,
                    tp.transport(),
                    tp.rank()
            ));
        }
    }

    // 출발 전 알림 설정
    private AlertMinute validateAndResolveAlertMinute(OnboardingRequest.NotificationSetting s) {
        if (!s.departAlertEnabled()) {
            // 비활성일 땐 값 의미 없지만 DB nullable=false라 기본값 고정
            return AlertMinute.MIN_10;
        }
        if (s.departAlertMinutes() == null) {
            throw new CustomException(OnboardingErrorCode.ONBOARDING_NOTIFICATION_MINUTE_REQUIRED);
        }
        return s.departAlertMinutes();
    }

    private void upsertNotificationSetting(User user, boolean enabled, AlertMinute minute) {
        Long userId = user.getId();

        notificationSettingRepo.findById(userId).ifPresentOrElse(
                existing -> existing.update(enabled, minute),
                () -> notificationSettingRepo.save(NotificationSetting.create(user, enabled, minute))
        );
    }
}
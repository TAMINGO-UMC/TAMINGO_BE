package app.tamingo.domain.onboarding.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.favoriteplace.dto.FavoritePlaceRequest;
import app.tamingo.domain.favoriteplace.service.FavoritePlaceService;
import app.tamingo.domain.notificationsetting.entity.AlertMinute;
import app.tamingo.domain.notificationsetting.service.NotificationSettingService;
import app.tamingo.domain.transportpreference.exception.TransportPreferenceErrorCode;
import app.tamingo.domain.useractivetime.exception.UserActiveTimeErrorCode;
import app.tamingo.domain.transportpreference.entity.TransportPreference;
import app.tamingo.domain.transportpreference.entity.TransportType;
import app.tamingo.domain.transportpreference.repository.TransportPreferenceRepository;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.onboarding.dto.OnboardingRequest;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.useractivetime.entity.UserActiveTime;
import app.tamingo.domain.useractivetime.repository.UserActiveTimeRepository;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import app.tamingo.domain.userlearning.entity.UserLearningSummary;
import app.tamingo.domain.userlearning.repository.PersonalSettingRepository;
import app.tamingo.domain.userlearning.repository.UserLearningSummaryRepository;
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

    private final UserActiveTimeRepository userActiveTimeRepository;
    private final TransportPreferenceRepository transportPreferenceRepository;
    private final PersonalSettingRepository personalSettingRepository;
    private final FavoritePlaceService favoritePlaceService;
    private final NotificationSettingService notificationSettingService;
    private final UserLearningSummaryRepository userLearningSummaryRepository;

    public void saveOnboarding(Long userId, OnboardingRequest req) {
        User user = getUserOrThrow(userId);

        ActiveTimeValue activeTime = validateAndParseActiveTime(req.activeTime());
        upsertActiveTime(user, activeTime, req.activeTime());

        List<FavoritePlaceRequest.SaveDto> places = toSaveDtos(req.favoritePlaces());
        favoritePlaceService.replaceAll(userId, places, MAX_FAVORITE_PLACES);

        validateTransportPreferences(req.transportPreferences());
        replaceTransportPreferences(user, req.transportPreferences());

        boolean enabled = req.notificationSetting().departAlertEnabled();
        AlertMinute minute = req.notificationSetting().departAlertMinutes();
        notificationSettingService.applyOnboarding(userId, enabled, minute);

        createPersonalSettingIfAbsent(user);

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
            throw new CustomException(UserActiveTimeErrorCode.TIME_ORDER_INVALID);
        }
        return new ActiveTimeValue(start, end);
    }

    private LocalTime parseTimeOrThrow(String s) {
        try {
            return LocalTime.parse(s); // expects "HH:mm"
        } catch (DateTimeParseException e) {
            throw new CustomException(UserActiveTimeErrorCode.TIME_FORMAT_INVALID);
        }
    }

    private void upsertActiveTime(User user, ActiveTimeValue v, OnboardingRequest.ActiveTime at) {
        Long userId = user.getId();

        userActiveTimeRepository.findById(userId).ifPresentOrElse(
                existing -> existing.update(
                        v.start(), v.end(),
                        at.monEnabled(),
                        at.tueEnabled(),
                        at.wedEnabled(),
                        at.thuEnabled(),
                        at.friEnabled(),
                        at.weekendEnabled()
                ),
                () -> userActiveTimeRepository.save(
                        UserActiveTime.of(
                                user,
                                v.start(), v.end(),
                                at.monEnabled(),
                                at.tueEnabled(),
                                at.wedEnabled(),
                                at.thuEnabled(),
                                at.friEnabled(),
                                at.weekendEnabled()
                        )
                )
        );
    }

    // 자주 가는 장소
    // 온보딩 DTO -> FavoritePlace 저장 DTO로 변환
    private List<FavoritePlaceRequest.SaveDto> toSaveDtos(List<OnboardingRequest.FavoritePlace> list) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream()
                .map(p -> new FavoritePlaceRequest.SaveDto(
                        p.name(),
                        p.address(),
                        p.latitude(),
                        p.longitude(),
                        false
                ))
                .toList();
    }

    // 선호 이동 수단
    private void validateTransportPreferences(List<OnboardingRequest.TransportPref> list) {
        if (list == null || list.size() != 3) {
            throw new CustomException(TransportPreferenceErrorCode.TRANSPORT_PREFERENCES_INVALID);
        }

        Set<TransportType> types = new HashSet<>();
        Set<Integer> ranks = new HashSet<>();

        for (OnboardingRequest.TransportPref tp : list) {
            if (tp.transport() == null) {
                throw new CustomException(TransportPreferenceErrorCode.TRANSPORT_PREFERENCES_INVALID);
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
            throw new CustomException(TransportPreferenceErrorCode.TRANSPORT_PREFERENCES_INVALID);
        }
    }

    private void replaceTransportPreferences(User user, List<OnboardingRequest.TransportPref> list) {
        Long userId = user.getId();

        transportPreferenceRepository.deleteAllByUserId(userId);
        for (OnboardingRequest.TransportPref tp : list) {
            transportPreferenceRepository.save(TransportPreference.of(
                    user,
                    tp.transport(),
                    tp.rank()
            ));
        }
    }

    // 개인화 설정(오차 로그 수집), 개인화 학습 설정
    private void createPersonalSettingIfAbsent(User user) {
        Long userId = user.getId();

        if (!personalSettingRepository.existsById(userId)) {
            personalSettingRepository.save(
                    PersonalSetting.of(user, true) // 기본값 ON
            );
            userLearningSummaryRepository.save(
                    UserLearningSummary.of(user,0,0.0,0)
            );
        }
    }
}
package app.tamingo.domain.transportpreference.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.transportpreference.dto.TransportPreferenceResponse;
import app.tamingo.domain.transportpreference.dto.TransportPreferenceUpdateRequest;
import app.tamingo.domain.transportpreference.entity.TransportPreference;
import app.tamingo.domain.transportpreference.entity.TransportType;
import app.tamingo.domain.transportpreference.exception.TransportPreferenceErrorCode;
import app.tamingo.domain.transportpreference.repository.TransportPreferenceRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class TransportPreferenceService {

    private final TransportPreferenceRepository repository;
    private final UserRepository userRepository;

    public TransportPreferenceResponse getPreferences(Long userId) {
        List<TransportPreference> preferences =
                repository.findAllByUserIdOrderByRankAsc(userId);

        // 없을 경우 default 값 설정
        if (preferences.isEmpty()) {
            return createDefaultPreferences(userId);
        }

        if (preferences.size() != 3) {
            throw new CustomException(
                    TransportPreferenceErrorCode.TRANSPORT_PREFERENCES_INVALID
            );
        }

        return new TransportPreferenceResponse(
                preferences.get(0).getTransport(),
                preferences.get(1).getTransport(),
                preferences.get(2).getTransport()
        );
    }

    public TransportPreferenceResponse updatePreferences(
            Long userId,
            TransportPreferenceUpdateRequest request
    ) {
        validateNoDuplicate(request);

        User user = userRepository.getReferenceById(userId);

        repository.deleteAllByUserId(userId);

        repository.saveAll(List.of(
                TransportPreference.of(user, request.getRank1(), 1),
                TransportPreference.of(user, request.getRank2(), 2),
                TransportPreference.of(user, request.getRank3(), 3)
        ));

        return new TransportPreferenceResponse(
                request.getRank1(),
                request.getRank2(),
                request.getRank3()
        );
    }

    private TransportPreferenceResponse createDefaultPreferences(Long userId) {
        User user = userRepository.getReferenceById(userId);

        repository.saveAll(List.of(
                TransportPreference.of(user, TransportType.SUBWAY, 1),
                TransportPreference.of(user, TransportType.BUS, 2),
                TransportPreference.of(user, TransportType.WALK, 3)
        ));

        return new TransportPreferenceResponse(
                TransportType.SUBWAY,
                TransportType.BUS,
                TransportType.WALK
        );
    }

    private void validateNoDuplicate(TransportPreferenceUpdateRequest request) {
        Set<TransportType> set = Set.of(
                request.getRank1(),
                request.getRank2(),
                request.getRank3()
        );

        if (set.size() != 3) {
            throw new CustomException(
                    TransportPreferenceErrorCode.TRANSPORT_PREFERENCES_INVALID
            );
        }
    }
}
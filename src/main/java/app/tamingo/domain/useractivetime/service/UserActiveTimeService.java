package app.tamingo.domain.useractivetime.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.common.response.ErrorCode;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.useractivetime.dto.UserActiveTimeRequest;
import app.tamingo.domain.useractivetime.dto.UserActiveTimeResponse;
import app.tamingo.domain.useractivetime.entity.UserActiveTime;
import app.tamingo.domain.useractivetime.exception.UserActiveTimeError;
import app.tamingo.domain.useractivetime.repository.UserActiveTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActiveTimeService {
    private final UserActiveTimeRepository userActiveTimeRepository;
    private final UserRepository userRepository;

    // 활동 시간 조회
    public UserActiveTimeResponse getUserActiveTime(Long userId) {

        // 유저 존재 여부 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 유저 설정 존재 여부 확인
        return userActiveTimeRepository.findById(userId)
                .map(UserActiveTimeResponse::from)
                .orElseGet(() -> {
                    return UserActiveTimeResponse.empty();
                });
    }

    // 활동 시간 지정, 수정
    @Transactional
    public UserActiveTimeResponse save(Long userId, UserActiveTimeRequest request) {

        // 시간 순서 검증
        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime())) {
            throw new CustomException(UserActiveTimeError.TIME_ORDER_INVALID);
        }

        // 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 설정 존재 확인
        UserActiveTime activeTime = userActiveTimeRepository.findById(userId)
                .map(existingTime -> {
                    existingTime.update(
                            request.startTime(), request.endTime(),
                            request.mon(), request.tue(), request.wed(),
                            request.thu(), request.fri(), request.weekend()
                    );
                    return existingTime;
                })
                .orElseGet(() -> {
                    UserActiveTime newSetting = UserActiveTime.of(
                            user, request.startTime(), request.endTime(),
                            request.mon(), request.tue(), request.wed(),
                            request.thu(), request.fri(), request.weekend()
                    );
                    return userActiveTimeRepository.save(newSetting);
                });
        return UserActiveTimeResponse.from(activeTime);
    }
}

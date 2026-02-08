package app.tamingo.domain.userlearning.service;

import app.tamingo.common.exception.CustomException;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.exception.UserErrorCode;
import app.tamingo.domain.user.repository.UserRepository;
import app.tamingo.domain.userlearning.dto.ErrorLogResponse;
import app.tamingo.domain.userlearning.dto.ErrorLogSettingResponse;
import app.tamingo.domain.userlearning.entity.ErrorLog;
import app.tamingo.domain.userlearning.entity.PersonalSetting;
import app.tamingo.domain.userlearning.exception.UserLearningErrorCode;
import app.tamingo.domain.userlearning.repository.ErrorLogRepository;
import app.tamingo.domain.userlearning.repository.PersonalSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ErrorLogService {

    private final PersonalSettingRepository personalSettingRepository;
    private final UserRepository userRepository;
    private final ErrorLogRepository errorLogRepository;

    // 오차 로그 설정 on off
    @Transactional
    public ErrorLogSettingResponse setErrorLogSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        PersonalSetting personalSetting = personalSettingRepository.findByUser(user);
        if (personalSetting == null){
            throw new CustomException(UserLearningErrorCode.PERSONAL_SETTING_NOT_FOUND);
        }
        // true & false update
        personalSetting.update();
        return new ErrorLogSettingResponse(personalSetting.isErrorLogEnabled());
    }

    // 오차 로그 설정 조회
    public ErrorLogSettingResponse viewErrorLogSetting(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        PersonalSetting personalSetting = personalSettingRepository.findByUser(user);
        if (personalSetting == null){
            throw new CustomException(UserLearningErrorCode.PERSONAL_SETTING_NOT_FOUND);
        }
        return new ErrorLogSettingResponse(personalSetting.isErrorLogEnabled());
    }

    // 오차 로그 상위 3개 조회
    public List<ErrorLogResponse> viewErrorLog(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        return errorLogRepository.findLatestByUserByNum(3,user)
                .stream()
                .map(log -> new ErrorLogResponse(
                        log.getDeparturePlace(),
                        log.getArrivalPlace(),
                        log.getExpectedDuration(),
                        log.getTotalDuration(),
                        log.getErrorMinutes()
                ))
                .toList();
    }

}

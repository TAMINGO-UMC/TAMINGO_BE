package app.tamingo.domain.schedule.exception;

import app.tamingo.common.exception.DataIntegrityMapper;
import app.tamingo.common.response.BaseCode;
import org.springframework.stereotype.Component;

@Component
public class ScheduleDataIntegrityMapper implements DataIntegrityMapper {

    // DataIntegrityMapper 참고 Override
    @Override
    public boolean supports(String key) {
        //유니크 키 설정 명칭을 다음과 같이 함
        return "uk_schedule_category_user_name".equals(key);
    }

    @Override
    public BaseCode errorCode() {
        //내보내고싶은 커스텀 에러코드 쓰시면 되요
        return ScheduleErrorCode.SCHEDULE_CATEGORY_DUPLICATED;
    }
}

package app.tamingo.common.exception;

import app.tamingo.common.response.BaseCode;


/**
 * 데이터 무결성 제약 조건이 터졌을때 이 매퍼 클래스를 상속 받으세요!! schedule/exception
 * 도메인에 있는 ScheduleDataIntegrityMapper 참고해서 작성 하면 됩니다.
 */
public interface DataIntegrityMapper {
    boolean supports(String key);
    BaseCode errorCode();
}

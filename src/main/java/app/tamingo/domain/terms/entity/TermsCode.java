package app.tamingo.domain.terms.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsCode {

    SERVICE("서비스 이용약관"),
    PRIVACY("개인정보 처리방침"),
    AI_SERVICE("AI 기반 서비스 이용약관"),
    LOCATION("위치 기반 서비스 이용약관"),
    MARKETING("마케팅 알림 수신 동의");

    private final String description;
}
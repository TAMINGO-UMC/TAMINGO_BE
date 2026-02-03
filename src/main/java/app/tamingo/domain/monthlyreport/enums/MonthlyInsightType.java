package app.tamingo.domain.monthlyreport.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MonthlyInsightType {
    PRODUCTIVITY,     // 생산성
    TIME_MANAGEMENT,  // 시간 관리
    CONSISTENCY,      // 꾸준함
    FOCUS,            // 집중
    HABIT             // 습관
}

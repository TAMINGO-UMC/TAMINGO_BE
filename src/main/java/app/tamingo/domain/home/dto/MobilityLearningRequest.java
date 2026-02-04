package app.tamingo.domain.home.dto;

import app.tamingo.domain.home.entity.enums.ArrivedStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이동 학습 데이터 수집용 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MobilityLearningRequest {

    /**
     * API 예측 시간 (분)
     */
    private int predictedMinutes;

    /**
     * 실제 소요 시간 (분)
     */
    private int actualMinutes;

    /**
     * 도착 상태
     */
    private ArrivedStatus arrivedStatus;

    /**
     * 출발 시각
     */
    private LocalDateTime departureTime;
}

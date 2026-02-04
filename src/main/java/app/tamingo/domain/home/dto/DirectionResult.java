package app.tamingo.domain.home.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 경로 조회 결과를 담는 DTO
 */
@Getter
@AllArgsConstructor
public class DirectionResult {

    /**
     * 총 소요 시간 (분)
     */
    private int totalMinutes;

    /**
     * 경로 좌표 리스트 (polyline)
     */
    private List<Location> polyline;

}

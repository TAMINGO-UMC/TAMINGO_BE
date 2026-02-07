package app.tamingo.domain.schedule.entity;
import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.schedule.enums.ScheduleResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "schedule_result",
        indexes = {
                /**
                 * 주간 리포트 배치에서 status 기반 count/avg를 자주 수행
                 * - 예: ON_TIME 비율, LATE 평균 지각분, NO_SHOW 수 등
                 */
                @Index(name = "idx_schedule_result_status", columnList = "status"),

                /**
                 * 결과 확정(평가) 시각 기준으로 최근 확정 데이터 조회/재평가에 활용 가능
                 */
                @Index(name = "idx_schedule_result_evaluated_at", columnList = "evaluated_at"),

                /**
                 * 네비게이션 보너스(B) 계산에서 navigation_used 조건 필터링에 도움
                 */
                @Index(name = "idx_schedule_result_navigation_used", columnList = "navigation_used")
        }
)
public class ScheduleResult extends BaseEntity {

    /**
     * PK = schedule_id
     * - 일정 1개당 결과 1개를 강제하기 위한 1:1 설계
     * - schedule_result가 중복 생성되는 것을 방지
     */
    @Id
    @Column(name = "schedule_id")
    private Long scheduleId;

    /**
     * 일정(Schedule)과 1:1 관계
     * - @MapsId로 Schedule PK를 그대로 결과 PK로 사용
     * - optional=false: 일정 없이 결과만 존재하는 것을 방지
     */
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    /**
     * 일정 수행 결과 상태
     * - ON_TIME / LATE / NO_SHOW / CANCELED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ScheduleResultStatus status;

    /**
     * 길찾기 시작 버튼 사용 여부
     *
     * 생산성 점수 공식의 B(Navigation Bonus) 계산 근거:
     * - 정책: "길찾기 시작" 버튼을 누른 후 이동하여 실제 도착(GPS 도착 확정)한 횟수당 +1점
     * - 즉, navigationUsed = true 이면서 status가 ON_TIME/LATE(도착)인 경우 1회로 카운트
     *
     * 주의:
     * - 길찾기 시작 버튼을 누르는 시점에 true로 업데이트(또는 최초 생성)
     * - 도착 확정은 GPS로 status가 ON_TIME/LATE가 되면서 판단
     */
    @Column(name = "navigation_used", nullable = false)
    private Boolean navigationUsed;

    /**
     * GPS 기반 실제 도착 확정 시각
     * - 도착이 확정된 경우에만 값이 존재(ON_TIME/LATE)
     * - 정시/지각 판정의 근거 데이터가 됨
     */
    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    /**
     * 지각 시간(분)
     * - status == LATE 인 경우에만 의미 있음
     * - 주간 리포트 avgLateMinutes 계산에 사용
     */
    @Column(name = "late_minutes")
    private Integer lateMinutes;

    /**
     * 정시 도착 점수(0~100)
     * - 주간 리포트 onTimeScore(P_score) 계산 근거(평균)
     * - 주간 리포트 onTimeRate(P_rate)는 punctualityScore == 100 비율로 계산 가능
     *
     * 예시 정책:
     * - ON_TIME: 100
     * - LATE: 100 - (lateMinutes * k) 또는 구간별 점수
     * - NO_SHOW: 0
     * - CANCELED: 0 또는 null(정책 선택) - 보통 분모에서 제외 권장
     */
    @Column(name = "punctuality_score", nullable = false)
    private Integer punctualityScore;

    /**
     * 결과 확정 시각
     * - GPS 도착 확정 시각 또는
     * - 미도착 확정(NO_SHOW) 배치/스케줄러 실행 시각
     */
    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;

    @Builder(builderMethodName = "internalBuilder")
    private ScheduleResult(
            Schedule schedule,
            ScheduleResultStatus status,
            Boolean navigationUsed,
            LocalDateTime arrivedAt,
            Integer lateMinutes,
            Integer punctualityScore,
            LocalDateTime evaluatedAt
    ) {
        this.schedule = schedule;
        this.status = status;
        this.navigationUsed = (navigationUsed != null) ? navigationUsed : false;
        this.arrivedAt = arrivedAt;
        this.lateMinutes = lateMinutes;
        this.punctualityScore = punctualityScore;
        this.evaluatedAt = evaluatedAt;
    }

    /**
     * 정적 팩토리 메서드
     * - 생성 규칙을 고정하고, 생성 시 기본값을 명확히 하기 위함
     */
    public static ScheduleResult of(
            Schedule schedule,
            ScheduleResultStatus status,
            Boolean navigationUsed,
            LocalDateTime arrivedAt,
            Integer lateMinutes,
            Integer punctualityScore,
            LocalDateTime evaluatedAt
    ) {
        return ScheduleResult.internalBuilder()
                .schedule(schedule)
                .status(status)
                .navigationUsed(navigationUsed)
                .arrivedAt(arrivedAt)
                .lateMinutes(lateMinutes)
                .punctualityScore(punctualityScore)
                .evaluatedAt(evaluatedAt)
                .build();
    }

    /**
     * 길찾기 시작 처리
     * - "길찾기 시작" 버튼을 눌렀을 때 호출
     * - 결과 row가 아직 없으면 생성 로직(서비스)에서 of()로 만들고 navigationUsed=true로 세팅
     */
    public void markNavigationUsed() {
        this.navigationUsed = true;
    }

    /**
     * GPS 도착 확정 처리
     * - 도착 확정 시 status/arrivedAt/lateMinutes/punctualityScore/evaluatedAt 갱신
     */
    public void confirmArrival(
            ScheduleResultStatus status, // ON_TIME 또는 LATE
            LocalDateTime arrivedAt,
            Integer lateMinutes,
            Integer punctualityScore,
            LocalDateTime evaluatedAt
    ) {
        this.status = status;
        this.arrivedAt = arrivedAt;
        this.lateMinutes = lateMinutes;
        this.punctualityScore = punctualityScore;
        this.evaluatedAt = evaluatedAt;
    }

    /**
     * 미도착 확정(NO_SHOW) 처리
     * - 일정 시작 후 cutoff 시간이 지났는데 도착이 확정되지 않은 경우 배치/스케줄러에서 호출
     */
    public void confirmNoShow(LocalDateTime evaluatedAt) {
        this.status = ScheduleResultStatus.NO_SHOW;
        this.arrivedAt = null;
        this.lateMinutes = null;
        this.punctualityScore = 0;
        this.evaluatedAt = evaluatedAt;
    }

    /**
     * 취소 확정 처리(CANCELED)
     * - 취소는 분모에서 제외하는 정책을 권장(리포트 계산 시 제외)
     */
    public void confirmCanceled(LocalDateTime evaluatedAt) {
        this.status = ScheduleResultStatus.CANCELED;
        this.evaluatedAt = evaluatedAt;
    }
}


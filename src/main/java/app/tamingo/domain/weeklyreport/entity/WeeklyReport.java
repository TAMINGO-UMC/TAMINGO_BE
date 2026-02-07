package app.tamingo.domain.weeklyreport.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.weeklyreport.enums.ProductivityGrade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "weekly_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_weekly_report_user_week",
                        columnNames = {"user_id", "week_start_date"}
                )
        },
        indexes = {
                @Index(name = "idx_weekly_report_user_week", columnList = "user_id, week_start_date")
        }
)
public class WeeklyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 리포트 소유자(User)
     * - 특정 사용자의 주간 리포트를 식별하기 위한 FK
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 리포트 집계 기간 시작일(주 시작일)
     * - 예: 2026-01-03
     * - 유니크키( user_id + week_start_date )의 일부
     */
    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    /**
     * 리포트 집계 기간 종료일(주 종료일)
     * - 예: 2026-01-09
     */
    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    /**
     * 정시 도착률 P_rate (%)
     * - "100점(정시 도착)"을 받은 일정 비율
     * - 공식: (100점 일정 수 / 전체 일정 수) * 100 (%)
     * - UI 상단 KPI: 정시 도착률(%) 표시용
     */
    @Column(name = "on_time_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal onTimeRate;

    /**
     * 정시 도착 점수 P_score (0~100)
     * - 지각/미도착까지 포함한 종합 시간관리 성취 점수(평균)
     * - 공식: (각 일정 점수의 합 / 전체 일정 수)
     * - 생산성 점수 계산에 직접 사용됨
     */
    @Column(name = "on_time_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal onTimeScore;

    /**
     * 지난 주 대비 정시 도착률 변화량(p.p)
     * - 공식: 이번 주 onTimeRate - 지난 주 onTimeRate
     * - UI: "+5% 상승" 같은 비교 문구용
     */
    @Column(name = "on_time_diff", precision = 5, scale = 2)
    private BigDecimal onTimeDiff;

    /**
     * 할 일 완료율 T_rate (%)
     * - 완료 체크된 할 일 비율
     * - 공식: (완료한 할 일 수 / 전체 할 일 수) * 100
     * - UI 상단 KPI: 할일 완료율(%) 표시용
     */
    @Column(name = "task_completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taskCompletionRate;

    /**
     * 할 일 완료 점수 T_score (0~100)
     * - 할 일 수행에 대한 점수화(평균)
     * - 완료=100, 미완료=0 이므로 평균은 일반적으로 완료율과 동일하지만
     *   정책상 "score" 개념을 분리해 저장 (추후 규칙 확장 대비)
     */
    @Column(name = "task_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal taskScore;

    /**
     * 지난 주 대비 할 일 완료율 변화량(p.p)
     * - 공식: 이번 주 taskCompletionRate - 지난 주 taskCompletionRate
     * - UI 비교 문구용
     */
    @Column(name = "task_completion_diff", precision = 5, scale = 2)
    private BigDecimal taskCompletionDiff;

    /**
     * 완료한 할 일 수 (분자)
     * - UI: "17/20개" 표기에서 17에 해당
     */
    @Column(name = "task_done_count", nullable = false)
    private Integer taskDoneCount;

    /**
     * 전체 할 일 수 (분모)
     * - UI: "17/20개" 표기에서 20에 해당
     */
    @Column(name = "task_total_count", nullable = false)
    private Integer taskTotalCount;

    /**
     * 최종 생산성 점수 Score (0~100)
     * - 정책 공식(최종):
     *   Score = min(100,
     *      ((P_score * 0.4) + (T_score * 0.4))
     *        * (1 + (D/7) * 0.2)
     *      + B
     *   )
     * - P_score: 주간 정시 도착 점수(일정 점수 평균)
     * - T_score: 주간 할 일 완료 점수(할 일 점수 평균)
     * - D: 활성 일수(1~7)  → (1 + (D/7)*0.2) 만큼 가중
     * - B: 길찾기 보너스(도착 횟수당 +1)
     * - UI: "생산성 점수 87점" 표시용
     */
    @Column(name = "productivity_score", nullable = false)
    private Integer productivityScore;

    /**
     * 생산성 등급
     * - 점수 구간에 따른 등급 표기
     * - EXCELLENT(80~100), GOOD(60~79), FAIR(40~59), LOW(<40)
     * - UI: "우수/적정/보통/낮음" 같은 라벨 표현용
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "productivity_grade", nullable = false, length = 20)
    private ProductivityGrade productivityGrade;

    /**
     * 활성 일수 D (0~7) 0은 아무 활동 안한걸로 침
     * - 일주일 중 타밍고를 통해 일정/할 일을 1개라도 처리한 날짜 수
     * - 생산성 점수 감점/가중 계산에 사용
     */
    @Column(name = "active_days", nullable = false)
    private Integer activeDays;

    /**
     * 길찾기 보너스 B
     * - "길찾기 시작 버튼" 사용 후 도착한 횟수
     * - 정책: 1회당 +1점 가산
     */
    @Column(name = "navigation_bonus", nullable = false)
    private Integer navigationBonus;

    /**
     * 평균 지각 시간(분)
     * - 지각(+3분 초과)으로 판정된 일정들의 지각 분 평균
     * - UI의 "평균 지각 시간 8분 → 3분" 같은 비교 지표에 활용 가능
     * - 정책/기획에 따라 null 허용
     */
    @Column(name = "avg_late_minutes")
    private Integer avgLateMinutes;

    /**
     * 요일별 활동 요약(월~일)
     * - 조회 시 주간 리포트 1개로 요일별 데이터를 함께 내려주기 위한 연관관계
     */
    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyActivitySummary> dailySummaries = new ArrayList<>();

    /**
     * 주간 인사이트 목록
     * - 주간 리포트 기반으로 생성된 메시지/경고/칭찬 카드 데이터
     */
    @OneToMany(mappedBy = "weeklyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeeklyInsight> insights = new ArrayList<>();

    @Builder(builderMethodName = "internalBuilder")
    private WeeklyReport(
            User user,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            BigDecimal onTimeRate,
            BigDecimal onTimeScore,
            BigDecimal onTimeDiff,
            BigDecimal taskCompletionRate,
            BigDecimal taskScore,
            BigDecimal taskCompletionDiff,
            Integer taskDoneCount,
            Integer taskTotalCount,
            Integer productivityScore,
            ProductivityGrade productivityGrade,
            Integer activeDays,
            Integer navigationBonus,
            Integer avgLateMinutes
    ) {
        this.user = user;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.onTimeRate = onTimeRate;
        this.onTimeScore = onTimeScore;
        this.onTimeDiff = onTimeDiff;
        this.taskCompletionRate = taskCompletionRate;
        this.taskScore = taskScore;
        this.taskCompletionDiff = taskCompletionDiff;
        this.taskDoneCount = taskDoneCount;
        this.taskTotalCount = taskTotalCount;
        this.productivityScore = productivityScore;
        this.productivityGrade = productivityGrade;
        this.activeDays = activeDays;
        this.navigationBonus = navigationBonus;
        this.avgLateMinutes = avgLateMinutes;
    }

    public static WeeklyReport of(
            User user,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            BigDecimal onTimeRate,
            BigDecimal onTimeScore,
            BigDecimal onTimeDiff,
            BigDecimal taskCompletionRate,
            BigDecimal taskScore,
            BigDecimal taskCompletionDiff,
            Integer taskDoneCount,
            Integer taskTotalCount,
            Integer productivityScore,
            ProductivityGrade productivityGrade,
            Integer activeDays,
            Integer navigationBonus,
            Integer avgLateMinutes
    ) {
        return WeeklyReport.internalBuilder()
                .user(user)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .onTimeRate(onTimeRate)
                .onTimeScore(onTimeScore)
                .onTimeDiff(onTimeDiff)
                .taskCompletionRate(taskCompletionRate)
                .taskScore(taskScore)
                .taskCompletionDiff(taskCompletionDiff)
                .taskDoneCount(taskDoneCount)
                .taskTotalCount(taskTotalCount)
                .productivityScore(productivityScore)
                .productivityGrade(productivityGrade)
                .activeDays(activeDays)
                .navigationBonus(navigationBonus)
                .avgLateMinutes(avgLateMinutes)
                .build();
    }

    //업데이트 메서드
    public void updateMetrics(
            LocalDate weekEndDate,
            BigDecimal onTimeRate,
            BigDecimal onTimeScore,
            BigDecimal onTimeDiff,
            BigDecimal taskCompletionRate,
            BigDecimal taskScore,
            BigDecimal taskCompletionDiff,
            Integer taskDoneCount,
            Integer taskTotalCount,
            Integer productivityScore,
            ProductivityGrade productivityGrade,
            Integer activeDays,
            Integer navigationBonus,
            Integer avgLateMinutes
    ) {
        this.weekEndDate = weekEndDate;
        this.onTimeRate = onTimeRate;
        this.onTimeScore = onTimeScore;
        this.onTimeDiff = onTimeDiff;
        this.taskCompletionRate = taskCompletionRate;
        this.taskScore = taskScore;
        this.taskCompletionDiff = taskCompletionDiff;
        this.taskDoneCount = taskDoneCount;
        this.taskTotalCount = taskTotalCount;
        this.productivityScore = productivityScore;
        this.productivityGrade = productivityGrade;
        this.activeDays = activeDays;
        this.navigationBonus = navigationBonus;
        this.avgLateMinutes = avgLateMinutes;
    }

}

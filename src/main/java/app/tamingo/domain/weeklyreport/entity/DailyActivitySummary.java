package app.tamingo.domain.weeklyreport.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.weeklyreport.enums.DayOfWeekType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "daily_activity_summary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_activity_summary_report_day",
                        columnNames = {"weekly_report_id", "day_of_week"}
                )
        },
        indexes = {
                @Index(name = "idx_daily_activity_summary_report", columnList = "weekly_report_id")
        }
)
public class DailyActivitySummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소속 주간 리포트 FK
     * - 한 주간 리포트(WeeklyReport)에 대해 요일별로 7개의 요약이 연결됨
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weekly_report_id", nullable = false)
    private WeeklyReport weeklyReport;

    /**
     * 요일
     * - 월~일 중 어떤 요약인지 식별
     * - (weekly_report_id, day_of_week) 유니크 보장
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeekType dayOfWeek;

    /**
     * 해당 요일의 일정 개수
     * - UI: "일정 5개" 표시용
     */
    @Column(name = "schedule_count", nullable = false)
    private Integer scheduleCount;

    /**
     * 해당 요일의 할 일 개수
     * - UI: "할일 3개" 표시용
     */
    @Column(name = "task_count", nullable = false)
    private Integer taskCount;

    /**
     * 요일별 정시 도착률 P_rate (%)
     * - 공식: (100점 일정 수 / 해당 요일 전체 일정 수) * 100
     */
    @Column(name = "punctuality_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal punctualityRate;

    /**
     * 요일별 할 일 완료율 T_rate (%)
     * - 공식: (해당 요일 완료한 할 일 수 / 해당 요일 전체 할 일 수) * 100
     */
    @Column(name = "task_completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taskCompletionRate;

    /**
     * 요일별 활동률 Rate (%)
     * - 정책 공식:
     *   Rate = (P_rate * 0.5) + (T_rate * 0.5)
     * - UI 진행바 퍼센트/텍스트 표시용
     */
    @Column(name = "activity_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal activityRate;

    @Builder(builderMethodName = "internalBuilder")
    private DailyActivitySummary(
            WeeklyReport weeklyReport,
            DayOfWeekType dayOfWeek,
            Integer scheduleCount,
            Integer taskCount,
            BigDecimal punctualityRate,
            BigDecimal taskCompletionRate,
            BigDecimal activityRate
    ) {
        this.weeklyReport = weeklyReport;
        this.dayOfWeek = dayOfWeek;
        this.scheduleCount = scheduleCount;
        this.taskCount = taskCount;
        this.punctualityRate = punctualityRate;
        this.taskCompletionRate = taskCompletionRate;
        this.activityRate = activityRate;
    }

    public static DailyActivitySummary of(
            WeeklyReport weeklyReport,
            DayOfWeekType dayOfWeek,
            Integer scheduleCount,
            Integer taskCount,
            BigDecimal punctualityRate,
            BigDecimal taskCompletionRate,
            BigDecimal activityRate
    ) {
        return DailyActivitySummary.internalBuilder()
                .weeklyReport(weeklyReport)
                .dayOfWeek(dayOfWeek)
                .scheduleCount(scheduleCount)
                .taskCount(taskCount)
                .punctualityRate(punctualityRate)
                .taskCompletionRate(taskCompletionRate)
                .activityRate(activityRate)
                .build();
    }
}

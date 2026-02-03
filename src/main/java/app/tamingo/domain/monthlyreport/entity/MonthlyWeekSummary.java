package app.tamingo.domain.monthlyreport.entity;

import app.tamingo.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "monthly_week_summary",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_monthly_week_summary_report_week",
                        columnNames = {"monthly_report_id", "week_index"}
                )
        },
        indexes = {
                @Index(name = "idx_monthly_week_summary_report", columnList = "monthly_report_id")
        }
)
public class MonthlyWeekSummary extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monthly_report_id", nullable = false)
    private MonthlyReport monthlyReport;

    @Column(name = "week_index", nullable = false)
    private Integer weekIndex; // 1~4

    @Column(name = "schedule_count", nullable = false)
    private Integer scheduleCount;

    @Column(name = "task_count", nullable = false)
    private Integer taskCount;

    @Column(name = "punctuality_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal punctualityRate;

    @Column(name = "task_completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taskCompletionRate;

    @Column(name = "activity_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal activityRate;

    @Builder(builderMethodName = "internalBuilder")
    private MonthlyWeekSummary(
            MonthlyReport monthlyReport,
            Integer weekIndex,
            Integer scheduleCount,
            Integer taskCount,
            BigDecimal punctualityRate,
            BigDecimal taskCompletionRate,
            BigDecimal activityRate
    ) {
        this.monthlyReport = monthlyReport;
        this.weekIndex = weekIndex;
        this.scheduleCount = scheduleCount;
        this.taskCount = taskCount;
        this.punctualityRate = punctualityRate;
        this.taskCompletionRate = taskCompletionRate;
        this.activityRate = activityRate;
    }

    public static MonthlyWeekSummary of(
            MonthlyReport monthlyReport,
            Integer weekIndex,
            Integer scheduleCount,
            Integer taskCount,
            BigDecimal punctualityRate,
            BigDecimal taskCompletionRate,
            BigDecimal activityRate
    ) {
        return MonthlyWeekSummary.internalBuilder()
                .monthlyReport(monthlyReport)
                .weekIndex(weekIndex)
                .scheduleCount(scheduleCount)
                .taskCount(taskCount)
                .punctualityRate(punctualityRate)
                .taskCompletionRate(taskCompletionRate)
                .activityRate(activityRate)
                .build();
    }
}

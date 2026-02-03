package app.tamingo.domain.monthlyreport.entity;

import app.tamingo.BaseEntity;
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
        name = "monthly_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_monthly_report_user_month",
                        columnNames = {"user_id", "month_start_date"}
                )
        },
        indexes = {
                @Index(name = "idx_monthly_report_user_month", columnList = "user_id, month_start_date")
        }
)
public class MonthlyReport extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "month_start_date", nullable = false)
    private LocalDate monthStartDate;

    @Column(name = "month_end_date", nullable = false)
    private LocalDate monthEndDate;

    @Column(name = "on_time_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal onTimeRate;

    @Column(name = "on_time_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal onTimeScore;

    @Column(name = "on_time_diff", precision = 5, scale = 2)
    private BigDecimal onTimeDiff;

    @Column(name = "task_completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taskCompletionRate;

    @Column(name = "task_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal taskScore;

    @Column(name = "task_completion_diff", precision = 5, scale = 2)
    private BigDecimal taskCompletionDiff;

    @Column(name = "task_done_count", nullable = false)
    private Integer taskDoneCount;

    @Column(name = "task_total_count", nullable = false)
    private Integer taskTotalCount;

    @Column(name = "productivity_score", nullable = false)
    private Integer productivityScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "productivity_grade", nullable = false, length = 20)
    private ProductivityGrade productivityGrade;

    @Column(name = "active_days", nullable = false)
    private Integer activeDays;

    @Column(name = "navigation_bonus", nullable = false)
    private Integer navigationBonus;

    @Column(name = "avg_late_minutes")
    private Integer avgLateMinutes;

    @OneToMany(mappedBy = "monthlyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonthlyWeekSummary> weekSummaries = new ArrayList<>();

    @OneToMany(mappedBy = "monthlyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonthlyInsight> insights = new ArrayList<>();

    @Builder(builderMethodName = "internalBuilder")
    private MonthlyReport(
            User user,
            LocalDate monthStartDate,
            LocalDate monthEndDate,
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
        this.monthStartDate = monthStartDate;
        this.monthEndDate = monthEndDate;
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

    public static MonthlyReport of(
            User user,
            LocalDate monthStartDate,
            LocalDate monthEndDate,
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
        return MonthlyReport.internalBuilder()
                .user(user)
                .monthStartDate(monthStartDate)
                .monthEndDate(monthEndDate)
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

    public void updateMetrics(
            LocalDate monthEndDate,
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
        this.monthEndDate = monthEndDate;
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

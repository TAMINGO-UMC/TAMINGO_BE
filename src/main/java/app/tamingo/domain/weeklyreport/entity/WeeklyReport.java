package app.tamingo.domain.weeklyreport.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        }
)
public class WeeklyReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 유저 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 주 시작 요일
    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    // 주 종료 요일
    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;

    // 정시 도착률 (%)
    @Column(name = "on_time_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal onTimeRate;

    // 지난 주 대비 정시 도착률 변화량 (p.p)
    @Column(name = "on_time_diff", precision = 5, scale = 2)
    private BigDecimal onTimeDiff;

    // 할 일 완료율 (%)
    @Column(name = "task_completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taskCompletionRate;

    // 지난 주 대비 할 일 완료율 변화량 (p.p)
    @Column(name = "task_completion_diff", precision = 5, scale = 2)
    private BigDecimal taskCompletionDiff;

    // 생산성 점수
    @Column(name = "productivity_score", nullable = false)
    private Integer productivityScore;

    @Builder(builderMethodName = "internalBuilder")
    private WeeklyReport(
            User user,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            BigDecimal onTimeRate,
            BigDecimal onTimeDiff,
            BigDecimal taskCompletionRate,
            BigDecimal taskCompletionDiff,
            Integer productivityScore
    ) {
        this.user = user;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.onTimeRate = onTimeRate;
        this.onTimeDiff = onTimeDiff;
        this.taskCompletionRate = taskCompletionRate;
        this.taskCompletionDiff = taskCompletionDiff;
        this.productivityScore = productivityScore;
    }

    public static WeeklyReport of(
            User user,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            BigDecimal onTimeRate,
            BigDecimal onTimeDiff,
            BigDecimal taskCompletionRate,
            BigDecimal taskCompletionDiff,
            Integer productivityScore
    ) {
        return WeeklyReport.internalBuilder()
                .user(user)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekEndDate)
                .onTimeRate(onTimeRate)
                .onTimeDiff(onTimeDiff)
                .taskCompletionRate(taskCompletionRate)
                .taskCompletionDiff(taskCompletionDiff)
                .productivityScore(productivityScore)
                .build();
    }
}

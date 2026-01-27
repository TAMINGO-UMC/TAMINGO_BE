package app.tamingo.domain.dailyactivity.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.dailyactivity.enums.DayOfWeekType;
import app.tamingo.domain.weeklyreport.entity.WeeklyReport;
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
        }
)
public class DailyActivitySummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주간 리포트 외래키
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weekly_report_id", nullable = false)
    private WeeklyReport weeklyReport;

    // 요일
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeekType dayOfWeek;

    // 해당 요일의 일정 수
    @Column(name = "schedule_count", nullable = false)
    private Integer scheduleCount;

    // 해당 요일의 할 일 수
    @Column(name = "task_count", nullable = false)
    private Integer taskCount;

    // 해당 요일의 완료된 일정 개수
    @Column(name = "schedule_done_count", nullable = false)
    private Integer scheduleDoneCount;

    // 일정 완료율 (%)
    @Column(
            name = "schedule_completion_rate",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal scheduleCompletionRate;

    @Builder(builderMethodName = "internalBuilder")
    private DailyActivitySummary(
            WeeklyReport weeklyReport,
            DayOfWeekType dayOfWeek,
            Integer scheduleCount,
            Integer taskCount,
            Integer scheduleDoneCount,
            BigDecimal scheduleCompletionRate
    ) {
        this.weeklyReport = weeklyReport;
        this.dayOfWeek = dayOfWeek;
        this.scheduleCount = scheduleCount;
        this.taskCount = taskCount;
        this.scheduleDoneCount = scheduleDoneCount;
        this.scheduleCompletionRate = scheduleCompletionRate;
    }

    public static DailyActivitySummary of(
            WeeklyReport weeklyReport,
            DayOfWeekType dayOfWeek,
            Integer scheduleCount,
            Integer taskCount,
            Integer scheduleDoneCount,
            BigDecimal scheduleCompletionRate
    ) {
        return DailyActivitySummary.internalBuilder()
                .weeklyReport(weeklyReport)
                .dayOfWeek(dayOfWeek)
                .scheduleCount(scheduleCount)
                .taskCount(taskCount)
                .scheduleDoneCount(scheduleDoneCount)
                .scheduleCompletionRate(scheduleCompletionRate)
                .build();
    }
}

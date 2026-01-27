package app.tamingo.domain.weeklyinsight.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.weeklyreport.entity.WeeklyReport;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "weekly_insight",
        indexes = {
                @Index(name = "idx_weekly_insight_weekly_report_id", columnList = "weekly_report_id")
        }
)
public class WeeklyInsight extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 주간 리포트 FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weekly_report_id", nullable = false)
    private WeeklyReport weeklyReport;

    // 인사이트 타입
    @Column(name = "type", nullable = false, length = 30)
    private String type;

    // 제목
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 내용
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    // 모델 버전
    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Builder(builderMethodName = "internalBuilder")
    private WeeklyInsight(
            WeeklyReport weeklyReport,
            String type,
            String title,
            String content,
            String modelVersion
    ) {
        this.weeklyReport = weeklyReport;
        this.type = type;
        this.title = title;
        this.content = content;
        this.modelVersion = modelVersion;
    }

    public static WeeklyInsight of(
            WeeklyReport weeklyReport,
            String type,
            String title,
            String content,
            String modelVersion
    ) {
        return WeeklyInsight.internalBuilder()
                .weeklyReport(weeklyReport)
                .type(type)
                .title(title)
                .content(content)
                .modelVersion(modelVersion)
                .build();
    }
}

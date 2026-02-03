package app.tamingo.domain.monthlyreport.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.monthlyreport.enums.MonthlyInsightType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "monthly_insight",
        indexes = {
                @Index(name = "idx_monthly_insight_monthly_report_id", columnList = "monthly_report_id")
        }
)
public class MonthlyInsight extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 월간 리포트 FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monthly_report_id", nullable = false)
    private MonthlyReport monthlyReport;

    // 인사이트 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private MonthlyInsightType type;

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
    private MonthlyInsight(
            MonthlyReport monthlyReport,
            MonthlyInsightType type,
            String title,
            String content,
            String modelVersion
    ) {
        this.monthlyReport = monthlyReport;
        this.type = type;
        this.title = title;
        this.content = content;
        this.modelVersion = modelVersion;
    }

    public static MonthlyInsight of(
            MonthlyReport monthlyReport,
            MonthlyInsightType type,
            String title,
            String content,
            String modelVersion
    ) {
        return MonthlyInsight.internalBuilder()
                .monthlyReport(monthlyReport)
                .type(type)
                .title(title)
                .content(content)
                .modelVersion(modelVersion)
                .build();
    }
}

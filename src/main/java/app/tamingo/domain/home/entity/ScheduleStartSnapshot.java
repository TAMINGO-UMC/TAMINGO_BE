package app.tamingo.domain.home.entity;

import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.home.entity.enums.StartSourceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "schedule_start_snapshot",
        indexes = {
                @Index(name = "idx_schedule_start_snapshot_schedule", columnList = "schedule_id")
        }
)
public class ScheduleStartSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 일정 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;


    @Enumerated(EnumType.STRING)
    @Column(name = "start_source_type", nullable = false)
    private StartSourceType startSourceType;

    /**
     * 출발지 근거 ID
     * - FVP: fvp_id
     * - PREV_SCHEDULE : 이전 schedule_id
     * - GPS : 0
     */
    @Column(name = "start_source_id", nullable = false)
    private Long startSourceId;

    // 실제 사용된 출발지 좌표
    @Column(name = "used_start_lat", nullable = false)
    private double usedStartLat;

    @Column(name = "used_start_lng", nullable = false)
    private double usedStartLng;

    // 출발지 결정 시점
    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    // silent gps에 의해 변경되었는지의 여부
    @Column(name = "is_overridden", nullable = false)
    private boolean overridden;
}

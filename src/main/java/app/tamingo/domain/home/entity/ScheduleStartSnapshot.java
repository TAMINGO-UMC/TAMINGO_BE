package app.tamingo.domain.home.entity;

import app.tamingo.domain.home.entity.enums.StartSourceType;
import app.tamingo.domain.schedule.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "schedule_start_snapshot",
        indexes = {
                @Index(name = "idx_schedule_start_snapshot_schedule", columnList = "schedule_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_schedule_start_snapshot_schedule", columnNames = "schedule_id")
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

    // 출발지 장소명 (예: FVP 이름, 이전 일정 장소명)
    @Column(name = "used_start_place_name")
    private String usedStartPlaceName;

    // 출발지 결정 시점
    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    // 스냅샷 기준 지도 ETA (분)
    @Column(name = "map_eta_minutes")
    private Integer mapEtaMinutes;

    // silent gps에 의해 변경되었는지의 여부
    @Column(name = "is_overridden", nullable = false)
    private boolean overridden;

    @Column(name = "is_reserved", nullable = false)
    private boolean isReserved = false;

    @Column(name = "expected_eta", nullable = false)
    private int expectedEta;

    public void reserved() {
        this.isReserved = true;
    }

    @Builder(builderMethodName = "internalBuilder")
    private ScheduleStartSnapshot(
            Schedule schedule,
            StartSourceType startSourceType,
            Long startSourceId,
            double usedStartLat,
            double usedStartLng,
            String usedStartPlaceName,
            LocalDateTime decidedAt,
            boolean overridden
    ) {
        this.schedule = schedule;
        this.startSourceType = startSourceType;
        this.startSourceId = startSourceId;
        this.usedStartLat = usedStartLat;
        this.usedStartLng = usedStartLng;
        this.usedStartPlaceName = usedStartPlaceName;
        this.decidedAt = decidedAt;
        this.overridden = overridden;
    }

    public static ScheduleStartSnapshot of(
            Schedule schedule,
            StartSourceType startSourceType,
            Long startSourceId,
            double usedStartLat,
            double usedStartLng,
            String usedStartPlaceName,
            LocalDateTime decidedAt,
            boolean overridden
    ) {
        return ScheduleStartSnapshot.internalBuilder()
                .schedule(schedule)
                .startSourceType(startSourceType)
                .startSourceId(startSourceId)
                .usedStartLat(usedStartLat)
                .usedStartLng(usedStartLng)
                .usedStartPlaceName(usedStartPlaceName)
                .decidedAt(decidedAt)
                .overridden(overridden)
                .build();
    }

    // GPS로 출발지 덮어쓰기
    public void overrideWithGps(
            double usedStartLat,
            double usedStartLng,
            String usedStartPlaceName,
            LocalDateTime decidedAt
    ) {
        applyGps(usedStartLat, usedStartLng, usedStartPlaceName, decidedAt, true);
    }

    // GPS 적용 공통 로직
    private void applyGps(
            double usedStartLat,
            double usedStartLng,
            String usedStartPlaceName,
            LocalDateTime decidedAt,
            boolean overridden
    ) {
        this.startSourceType = StartSourceType.GPS;
        this.startSourceId = 0L;
        this.usedStartLat = usedStartLat;
        this.usedStartLng = usedStartLng;
        this.decidedAt = decidedAt;
        this.usedStartPlaceName = usedStartPlaceName;
        this.overridden = overridden;
        this.mapEtaMinutes = null;
    }

    public void updateMapEtaMinutes(Integer mapEtaMinutes) {
        this.mapEtaMinutes = mapEtaMinutes;
    }

}

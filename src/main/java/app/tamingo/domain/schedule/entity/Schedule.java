package app.tamingo.domain.schedule.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.todo.entity.Todo;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "schedule",
        indexes = {
                @Index(name = "idx_schedule_user", columnList = "user_id"),
                // 날짜별(월간/주간) 조회
                @Index(name = "idx_schedule_period", columnList = "start_time, end_time"),
                // 자주 가는 장소 카운트 조회
                @Index(name = "idx_schedule_place", columnList = "place_name")
        }
)

@SQLDelete(sql = "UPDATE schedule SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_category_id")
    private ScheduleCategory scheduleCategory;

    @OneToMany(mappedBy = "schedule")
    private List<Todo> todoList = new ArrayList<>();

    @Column(name = "title" , nullable = false, length = 20)
    private String title;

    // 받을 때는 날짜와 시간 따로, 저장할 때는 합쳐서 DATETIME 으로 저장
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // 지도 ETA (분 단위, null 가능)
    @Column(name = "map_eta_minutes")
    private Integer mapEtaMinutes;

    // 길찾기 기능 해제 여부, 기본 true로 설정
    @Column(name = "is_navigation_enabled", nullable = false)
    private Boolean isNavigationEnabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType = RepeatType.NONE;

    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;

    @Column(name = "memo", length = 200)
    private String memo;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder(builderMethodName = "internalBuilder")
    private Schedule(
            User user,
            ScheduleCategory scheduleCategory,
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            RepeatType repeatType,
            LocalDate repeatEndDate,
            String memo
    ) {
        this.user = user;
        this.scheduleCategory = scheduleCategory;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.memo = memo;

        // RepeatType 설정 및 EndDate 정제
        this.repeatType = (repeatType != null) ? repeatType : RepeatType.NONE;

        // NONE이면 날짜가 들어와도 무시하고 null 저장
        this.repeatEndDate = (this.repeatType == RepeatType.NONE) ? null : repeatEndDate;
    }

    public static Schedule of(
            User user,
            ScheduleCategory scheduleCategory,
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            RepeatType repeatType,
            LocalDate repeatEndDate,
            String memo
    ) {
        return Schedule.internalBuilder()
                .user(user)
                .scheduleCategory(scheduleCategory)
                .title(title)
                .startTime(startTime)
                .endTime(endTime)
                .placeName(placeName)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .repeatType(repeatType)
                .repeatEndDate(repeatEndDate)
                .memo(memo)
                .build();
    }

    // 길찾기 기능 해제
    public void disableNavigation() {
        this.isNavigationEnabled = false;
    }

    public void update(
            ScheduleCategory scheduleCategory,
            String title,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            RepeatType repeatType,
            LocalDate repeatEndDate,
            String memo
    ) {
        this.scheduleCategory = scheduleCategory;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.memo = memo;

        this.repeatType = (repeatType != null) ? repeatType : RepeatType.NONE;
        this.repeatEndDate = (this.repeatType == RepeatType.NONE) ? null : repeatEndDate;
    }

    public void updateMapEtaMinutes(Integer mapEtaMinutes) {
        this.mapEtaMinutes = mapEtaMinutes;
    }

    public void softDelete(LocalDateTime now) {
        if (this.deletedAt != null) {
            return;
        }
        this.deletedAt = now;
    }
}

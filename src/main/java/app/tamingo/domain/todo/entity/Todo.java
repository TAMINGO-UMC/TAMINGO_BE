package app.tamingo.domain.todo.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name="todo",
        indexes = {
                @Index(name = "idx_todo_user", columnList = "user_id"),
                @Index(name = "idx_todo_schedule", columnList = "schedule_id"),
                @Index(name = "idx_todo_place", columnList = "place_name")
        }
)
public class Todo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_category_id")
    private TodoCategory todoCategory;

    @Column(name = "title", nullable = false,length = 20)
    private String title;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "duration")
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType= RepeatType.NONE;

    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;

    @Column(name = "is_checked", nullable = false)
    private boolean isChecked = false;

    @Column(name = "is_location_confirmed", nullable = false)
    private boolean isLocationConfirmed = false;

    @Builder(builderMethodName = "internalBuilder")
    private Todo(
            User user,
            Schedule schedule,
            TodoCategory todoCategory,
            String title,
            LocalDate targetDate,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            Integer duration,
            RepeatType repeatType,
            LocalDate repeatEndDate,
            boolean isChecked,
            boolean isLocationConfirmed
    ) {
        this.user = user;
        this.schedule = schedule;
        this.todoCategory = todoCategory;
        this.title = title;
        this.targetDate = targetDate;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.duration = duration;
        this.isChecked = isChecked;
        this.isLocationConfirmed = isLocationConfirmed;

        // RepeatType이 NONE이면 repeatEndDate는 무조건 null
        this.repeatType = (repeatType != null) ? repeatType : RepeatType.NONE;
        this.repeatEndDate = (this.repeatType == RepeatType.NONE) ? null : repeatEndDate;
    }

    public static Todo of(
            User user,
            TodoCategory todoCategory,
            String title,
            LocalDate targetDate,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            Integer duration,
            RepeatType repeatType,
            LocalDate repeatEndDate
    ) {
        return Todo.internalBuilder()
                .user(user)
                .todoCategory(todoCategory)
                .title(title)
                .targetDate(targetDate)
                .placeName(placeName)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .duration(duration)
                .repeatType(repeatType)
                .repeatEndDate(repeatEndDate)
                .schedule(null)            // 생성 시점엔 스케줄 연결 안 됨
                .isChecked(false)          // 생성 시 기본값 false
                .isLocationConfirmed(false)// 생성 시 기본값 false
                .build();
    }

    // 일정 연결 후 날짜를 일정 시작일로 동기화
    public void connectSchedule(Schedule schedule){
        this.schedule = schedule;
        // 일정이 연결되면 할 일의 날짜는 일정의 시작 날짜를 따라감
        if (schedule != null) {
            this.targetDate = schedule.getStartTime().toLocalDate();
        }
    }

    // 연결 해제
    public void disconnectSchedule() {
        this.schedule = null;
    }

    public void update(
            String title,
            LocalDate targetDate,
            TodoCategory todoCategory,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            Integer duration,
            RepeatType repeatType,
            LocalDate repeatEndDate
    ) {
        this.title = title;
        this.targetDate = targetDate;
        this.todoCategory = todoCategory;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.duration = duration;

        // 반복 정보 갱신 (NONE이면 날짜 null 강제)
        this.repeatType = (repeatType != null) ? repeatType : RepeatType.NONE;
        this.repeatEndDate = (this.repeatType == RepeatType.NONE) ? null : repeatEndDate;

        // 직접 수정/저장했으므로 true 저장
        this.isLocationConfirmed = true;
    }

    public void updateCheckStatus(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public void updateTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    // 반복 할 일 생성용
    public static Todo createRecurring(
            User user,
            TodoCategory todoCategory,
            String title,
            LocalDate targetDate,
            String placeName,
            String address,
            Double latitude,
            Double longitude,
            Integer duration,
            RepeatType repeatType,
            LocalDate repeatEndDate,
            boolean isLocationConfirmed // ★ 파라미터 추가됨
    ) {
        return Todo.internalBuilder()
                .user(user)
                .todoCategory(todoCategory)
                .title(title)
                .targetDate(targetDate)
                .placeName(placeName)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .duration(duration)
                .repeatType(repeatType)
                .repeatEndDate(repeatEndDate)
                .schedule(null)
                .isChecked(false)
                .isLocationConfirmed(isLocationConfirmed) // ★ 받은 값(true)으로 설정
                .build();
    }
}

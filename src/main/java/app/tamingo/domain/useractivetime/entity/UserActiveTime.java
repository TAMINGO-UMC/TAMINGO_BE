package app.tamingo.domain.useractivetime.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name="user_active_time")
public class UserActiveTime extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    private LocalTime startTime = LocalTime.of(9,0);
    @Builder.Default
    private LocalTime endTime = LocalTime.of(22,0);

    // 요일 필드
    @Builder.Default
    private boolean mon = true;
    @Builder.Default
    private boolean tue = true;
    @Builder.Default
    private boolean wed = true;
    @Builder.Default
    private boolean thu = true;
    @Builder.Default
    private boolean fri = true;
    @Builder.Default
    private boolean weekend = true;

    private UserActiveTime(User user, LocalTime startTime, LocalTime endTime, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean weekend) {
        this.user = user;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.weekend = weekend;
    }

    public static UserActiveTime of(User user, LocalTime startTime, LocalTime endTime, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean weekend) {
        return UserActiveTime.builder()
                .user(user)
                .startTime(startTime)
                .endTime(endTime)
                .mon(mon)
                .tue(tue)
                .wed(wed)
                .thu(thu)
                .fri(fri)
                .weekend(weekend)
                .build();
    }

    // 업데이트 더티체킹
    public void update(LocalTime startTime, LocalTime endTime, boolean mon, boolean tue, boolean wed, boolean thu, boolean fri, boolean weekend) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.weekend = weekend;
    }
}

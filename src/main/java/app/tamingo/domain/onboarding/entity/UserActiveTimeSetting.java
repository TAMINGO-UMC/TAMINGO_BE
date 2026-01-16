package app.tamingo.domain.onboarding.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "user_active_time_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActiveTimeSetting extends BaseEntity {

    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "mon_enabled", nullable = false)
    private boolean monEnabled;

    @Column(name = "tue_enabled", nullable = false)
    private boolean tueEnabled;

    @Column(name = "wed_enabled", nullable = false)
    private boolean wedEnabled;

    @Column(name = "thu_enabled", nullable = false)
    private boolean thuEnabled;

    @Column(name = "fri_enabled", nullable = false)
    private boolean friEnabled;

    @Column(name = "weekend_enabled", nullable = false)
    private boolean weekendEnabled;

    public static UserActiveTimeSetting create(
            User user,
            LocalTime startTime,
            LocalTime endTime,
            boolean monEnabled,
            boolean tueEnabled,
            boolean wedEnabled,
            boolean thuEnabled,
            boolean friEnabled,
            boolean weekendEnabled
    ) {
        UserActiveTimeSetting s = new UserActiveTimeSetting();
        s.user = user;
        s.userId = user.getId();
        s.startTime = startTime;
        s.endTime = endTime;
        s.monEnabled = monEnabled;
        s.tueEnabled = tueEnabled;
        s.wedEnabled = wedEnabled;
        s.thuEnabled = thuEnabled;
        s.friEnabled = friEnabled;
        s.weekendEnabled = weekendEnabled;
        return s;
    }

    public void update(
            LocalTime startTime,
            LocalTime endTime,
            boolean monEnabled,
            boolean tueEnabled,
            boolean wedEnabled,
            boolean thuEnabled,
            boolean friEnabled,
            boolean weekendEnabled
    ) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.monEnabled = monEnabled;
        this.tueEnabled = tueEnabled;
        this.wedEnabled = wedEnabled;
        this.thuEnabled = thuEnabled;
        this.friEnabled = friEnabled;
        this.weekendEnabled = weekendEnabled;
    }
}
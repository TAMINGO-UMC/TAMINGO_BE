package app.tamingo.domain.todo.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.schedule.entity.Schedule;
import app.tamingo.domain.todo.enums.RepeatType;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="todo")
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

    @Column(name = "title", nullable = false,length = 20)
    private String title;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "place_name")
    private String placeName;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    @Builder.Default
    private RepeatType repeatType= RepeatType.NONE;

    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;

    @Column(name = "is_checked", nullable = false)
    @Builder.Default
    private boolean isChecked = false;

}

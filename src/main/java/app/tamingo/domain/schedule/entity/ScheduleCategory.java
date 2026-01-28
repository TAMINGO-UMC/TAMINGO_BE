package app.tamingo.domain.schedule.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "schedule_category",
        indexes = {
                @Index(name = "idx_schedule_category_user_id", columnList = "user_id")
        }
)
public class ScheduleCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 카테고리 이름
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // 아이콘 코드
    @Column(name = "icon_code", nullable = false, length = 50)
    private String iconCode;

    // 색상 코드
    @Column(name = "color_code", nullable = false, length = 20)
    private String colorCode;

    //빌더형식 생성자
    @Builder(builderMethodName = "internalBuilder")
    private ScheduleCategory(String name, String iconCode, String colorCode, User user) {
        this.name = name;
        this.iconCode = iconCode;
        this.colorCode = colorCode;
        this.user = user;
    }

    //정적팩토리 메서드
    public static ScheduleCategory of(String name, String iconCode, String colorCode, User user) {
        return ScheduleCategory.internalBuilder()
                .name(name)
                .iconCode(iconCode)
                .colorCode(colorCode)
                .user(user)
                .build();
    }
    //수정 기능
    public void update(String name, String iconCode, String colorCode) {
        this.name = name;
        this.iconCode = iconCode;
        this.colorCode = colorCode;
    }

}

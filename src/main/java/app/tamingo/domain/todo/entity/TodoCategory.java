package app.tamingo.domain.todo.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "todo_category",
        indexes = {
                @Index(name = "idx_todo_category_user_id", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_todo_category_user_name",
                        columnNames = {"user_id", "name"}
                )
        }
)
public class TodoCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    //유저 FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    //카테고리 이름
    @Column(name = "name", nullable = false, length = 50)
    private String name;


    //색상 코드
    @Column(name = "color_code", nullable = false, length = 20)
    private String colorCode;

    //빌더형식 생성자
    @Builder(builderMethodName = "internalBuilder")
    private TodoCategory(String name,String colorCode, User user) {
        this.name = name;
        this.colorCode = colorCode;
        this.user = user;
    }

    //정적팩토리 메서드
    public static TodoCategory of(String name,String colorCode, User user) {

        return TodoCategory.internalBuilder()
                .name(name)
                .colorCode(colorCode)
                .user(user)
                .build();
    }
    //수정 메서드
    public void update(String name,String colorCode) {
        this.name = name;
        this.colorCode = colorCode;
    }
}

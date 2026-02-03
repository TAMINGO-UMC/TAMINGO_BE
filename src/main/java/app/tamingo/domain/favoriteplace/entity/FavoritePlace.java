package app.tamingo.domain.favoriteplace.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name="favorite_place_standard",
        indexes = {
                // 유저별 장소 조회시 인덱스
                @Index(name = "idx_favorite_place_user_id", columnList = "user_id")
        }
)
public class FavoritePlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 유저 아이디 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 장소의 이름
    @Column(nullable = false, length = 255)
    private String name;

    // 장소 주소
    @Column(nullable = false, length = 500)
    private String address;

    // 장소의 위도
    private Double latitude;

    // 장소의 경도
    private Double longitude;

    @Builder
    private FavoritePlace(User user, String name, String address, Double latitude, Double longitude) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static FavoritePlace of(User user, String name, String address, Double latitude, Double longitude) {
        return FavoritePlace.builder()
                .user(user)
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }

    // 업데이트 더티체킹
    public void update(String name, String address, Double latitude, Double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}

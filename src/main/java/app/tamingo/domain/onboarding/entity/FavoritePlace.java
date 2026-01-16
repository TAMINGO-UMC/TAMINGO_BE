package app.tamingo.domain.onboarding.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favorite_place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoritePlace extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    private Double latitude;
    private Double longitude;

    public static FavoritePlace create(User user, String name, String address, Double latitude, Double longitude) {
        FavoritePlace p = new FavoritePlace();
        p.user = user;
        p.name = name;
        p.address = address;
        p.latitude = latitude;
        p.longitude = longitude;
        return p;
    }
}
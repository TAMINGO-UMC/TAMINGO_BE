package app.tamingo.domain.onboarding.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Builder(builderMethodName = "internalBuilder")
    private FavoritePlace(
            User user,
            String name,
            String address,
            Double latitude,
            Double longitude
    ) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static FavoritePlace of(User user, String name, String address, Double latitude, Double longitude) {
        return FavoritePlace.internalBuilder()
                .user(user)
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
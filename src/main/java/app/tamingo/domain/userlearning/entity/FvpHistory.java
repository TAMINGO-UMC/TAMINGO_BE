package app.tamingo.domain.userlearning.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.userlearning.entity.enums.FvpType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fvp_history",
        indexes = {
                @Index(name = "idx_fvp_user", columnList = "user_id")
        })
public class FvpHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "latitude", nullable = false)
    private double latitude;

    @Column(name = "longitude", nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "fvp_type", nullable = false)
    private FvpType fvpType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder(builderMethodName = "internalBuilder")
    private FvpHistory(
            User user,
            String name,
            double latitude,
            double longitude,
            FvpType fvpType,
            LocalDateTime createdAt) {
        this.user = user;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fvpType = fvpType;
    }

    public static FvpHistory of(
            User user,
            String name,
            double latitude,
            double longitude,
            FvpType fvpType
    ) {
        return FvpHistory.internalBuilder()
                .user(user)
                .name(name)
                .latitude(latitude)
                .longitude(longitude)
                .fvpType(fvpType)
                .build();
    }
}

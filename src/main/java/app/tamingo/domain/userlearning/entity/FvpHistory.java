package app.tamingo.domain.userlearning.entity;

import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fvp_history",
        indexes = {
                @Index(name = "idx_fvp_user", columnList = "user_id")
        })
public class FvpHistory {

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
}

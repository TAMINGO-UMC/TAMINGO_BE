package app.tamingo.domain.transportpreference.entity;

import app.tamingo.common.entity.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "transport_preferences",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_transport_rank", columnNames = {"user_id", "rank"}),
                @UniqueConstraint(name = "uk_transport_type", columnNames = {"user_id", "transport"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransportPreference extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransportType transport;

    @Column(nullable = false)
    private int rank;

    @Builder(builderMethodName = "internalBuilder")
    private TransportPreference(
            User user,
            TransportType transport,
            int rank
    ) {
        this.user = user;
        this.transport = transport;
        this.rank = rank;
    }

    public static TransportPreference of(User user, TransportType transport, int rank) {
        return TransportPreference.internalBuilder()
                .user(user)
                .transport(transport)
                .rank(rank)
                .build();
    }
}
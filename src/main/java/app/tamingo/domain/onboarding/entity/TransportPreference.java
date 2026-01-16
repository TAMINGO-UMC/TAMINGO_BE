package app.tamingo.domain.onboarding.entity;

import app.tamingo.BaseEntity;
import app.tamingo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    public static TransportPreference create(User user, TransportType transport, int rank) {
        TransportPreference p = new TransportPreference();
        p.user = user;
        p.transport = transport;
        p.rank = rank;
        return p;
    }
}
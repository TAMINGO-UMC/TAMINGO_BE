package app.tamingo.domain.terms.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "terms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 약관 식별 코드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 50)
    private TermsCode code;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = true) // 개발 초기 단계 -> 허용
    private String content;

    @Column(name = "is_required", nullable = false)
    private boolean isRequired;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(name = "effective_at", nullable = false)
    private OffsetDateTime effectiveAt;
}
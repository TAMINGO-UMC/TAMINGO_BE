package app.tamingo.dev.userseeder;

import app.tamingo.domain.auth.entity.AuthIdentity;
import app.tamingo.domain.auth.repository.AuthIdentityRepository;
import app.tamingo.domain.user.entity.User;
import app.tamingo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuthIdentityRepository authIdentityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String email = "master@tamingo.dev";
        String rawPassword = "Test1234!";

        // 1) users 테이블에 유저 생성(비번은 없음)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.of(email, "마스터")));

        // 2) auth_identities에 LOCAL 계정 생성(비번 해시 저장)
        boolean hasLocalIdentity = authIdentityRepository.existsByProviderAndEmail(
                app.tamingo.domain.auth.entity.AuthProvider.LOCAL, email
        );

        if (!hasLocalIdentity) {
            String hash = passwordEncoder.encode(rawPassword);
            AuthIdentity identity = AuthIdentity.createLocal(user, email, hash);
            authIdentityRepository.save(identity);
        }

        System.out.println("[DEV] master user ready: " + email + " / " + rawPassword);
    }
}

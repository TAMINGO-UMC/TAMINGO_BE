package app.tamingo.domain.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;

@Slf4j
@Configuration
public class FcmConfig {

    private final String fcmKeyPath;
    private final ResourceLoader resourceLoader;

    public FcmConfig(
            @Value("${fcm.key.path:classpath:serviceAccountKey.json}") String fcmKeyPath,
            ResourceLoader resourceLoader
    ) {
        this.fcmKeyPath = fcmKeyPath;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(
                            resourceLoader.getResource(fcmKeyPath).getInputStream()))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화: {}", fcmKeyPath);
            }
        } catch (IOException e) {
            log.error("Firebase 초기화 실패: {}", e.getMessage());
            throw new RuntimeException("FCM 설정 오류");
        }
    }
}

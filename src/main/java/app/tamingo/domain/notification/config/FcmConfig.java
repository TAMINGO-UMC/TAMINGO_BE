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
            org.springframework.core.io.Resource resource = resourceLoader.getResource(fcmKeyPath);

            if (resource.exists()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("FCM 초기화");
                }
            } else {
                log.error("FCM 키 파일을 찾을 수 없습니다. 경로 확인 필요: {}", fcmKeyPath);
            }
        } catch (Exception e) {
            log.error("FCM 초기화 중 에러 발생: {}", e.getMessage());
        }
    }
}

package app.tamingo.domain.auth.service;

import app.tamingo.domain.auth.entity.EmailVerification;
import app.tamingo.domain.auth.redis.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final long EMAIL_CODE_TTL = 300L;

    private final EmailVerificationRepository repository;
    private final EmailSender emailSender;

    public void sendCode(String email) {
        String code = generateCode();
        repository.save(EmailVerification.of(email, code, EMAIL_CODE_TTL));
        emailSender.send(email, code);
    }

    public boolean verifyCode(String email, String inputCode) {
        if (email == null || inputCode == null) return false;
        if (inputCode.isBlank()) return false;

        EmailVerification ev = repository.findById(email).orElse(null);
        if (ev == null) return false;
        if (!ev.getCode().equals(inputCode)) return false;

        repository.deleteById(email);
        return true;
    }

    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}
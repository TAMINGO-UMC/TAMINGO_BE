package app.tamingo.domain.auth.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void send(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("[TAMINGO] 이메일 인증번호");
        message.setText(buildBody(code));

        mailSender.send(message);
    }

    private String buildBody(String code) {
        return """
                안녕하세요! TAMINGO 입니다.

                아래 인증번호를 입력해 이메일 인증을 완료해주세요.

                인증번호: %s

                * 인증번호는 5분간 유효합니다.
                """.formatted(code);
    }
}
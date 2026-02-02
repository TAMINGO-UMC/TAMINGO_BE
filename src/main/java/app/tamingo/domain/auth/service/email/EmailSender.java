package app.tamingo.domain.auth.service.email;

public interface EmailSender {
    void send(String toEmail, String code);
}

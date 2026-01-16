package app.tamingo.domain.auth.service;

public interface EmailSender {
    void send(String toEmail, String code);
}

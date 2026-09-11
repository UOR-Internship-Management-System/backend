package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

/**
 * Adapter boundary for sending transactional email.
 *
 * <p>Both a plain-text and an HTML body are always supplied. Implementations should send a
 * multipart/alternative message so clients that render HTML get the styled version and clients
 * that don't fall back to plain text.
 */
public interface EmailSender {

    void send(String recipientEmail, String subject, String textBody, String htmlBody);
}

package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

/** Contract for sending transactional emails. */
public interface EmailSender {

    /**
     * Sends a single transactional message.
     *
     * @throws EmailDeliveryException when the message could not be handed to the transport.
     */
    void send(String recipientEmail, String subject, String body);
}

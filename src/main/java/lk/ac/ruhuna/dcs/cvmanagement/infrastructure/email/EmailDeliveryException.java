package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

/**
 * Raised when a transactional email cannot be handed to the mail transport.
 *
 * <p>This is deliberately unchecked and deliberately propagated: OTP delivery failure must roll
 * back the surrounding verification transaction so the caller is never told an OTP was sent when
 * it was not.
 */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

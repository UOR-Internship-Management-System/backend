package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.email;

import java.time.Instant;

public interface OtpEmailSender {

    void sendOtp(String recipientEmail, String purpose, String otp, Instant expiresAt);
}

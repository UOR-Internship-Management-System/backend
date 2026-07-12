package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response;

import java.time.Instant;
import java.util.UUID;

public record StudentVerificationResponse(
        UUID verificationId,
        String status,
        String message,
        Instant expiresAt) {
}

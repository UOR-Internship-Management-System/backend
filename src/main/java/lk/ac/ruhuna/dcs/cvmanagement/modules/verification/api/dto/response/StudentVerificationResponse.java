package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response;

import java.util.UUID;

public record StudentVerificationResponse(
        UUID verificationId,
        String message,
        long expiresInSeconds) {
}

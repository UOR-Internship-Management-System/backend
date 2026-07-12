package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response;

import java.util.UUID;

public record PasswordResetResponse(
        UUID resetId,
        String message,
        long expiresInSeconds) {
}

package lk.ac.ruhuna.dcs.cvmanagement.modules.verification.api.dto.response;

public record OtpResendResponse(
        String message,
        long expiresInSeconds) {
}

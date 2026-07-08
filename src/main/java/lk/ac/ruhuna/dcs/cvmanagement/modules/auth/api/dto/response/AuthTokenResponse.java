package lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.response;

public record AuthTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        CurrentUserResponse user) {
}

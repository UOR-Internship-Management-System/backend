package lk.ac.ruhuna.dcs.cvmanagement.modules.auth.api.dto.response;

import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(
        UUID userId,
        UUID accountId,
        String email,
        String displayName,
        Set<String> roles,
        String primaryRole) {
}

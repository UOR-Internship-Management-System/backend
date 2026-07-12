package lk.ac.ruhuna.dcs.cvmanagement.shared.security;

import java.util.Set;
import java.util.UUID;

public record CurrentActor(
        UUID userId,
        String email,
        Set<RoleName> roles) {

    public boolean hasRole(RoleName role) {
        return roles.contains(role);
    }
}

package lk.ac.ruhuna.dcs.cvmanagement.shared.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentActorProvider {

    public Optional<CurrentActor> currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CurrentActor actor)) {
            return Optional.empty();
        }
        return Optional.of(actor);
    }
}

package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.jwt;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtProperties properties;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.secret() != null && properties.secret().length() >= 32;
    }
}

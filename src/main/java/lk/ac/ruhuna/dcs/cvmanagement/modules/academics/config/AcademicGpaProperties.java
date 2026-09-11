package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config;

import java.math.RoundingMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Centralized output precision policy for persisted Computer Science GPA values. */
@ConfigurationProperties(prefix = "app.academics.gpa")
public record AcademicGpaProperties(RoundingMode roundingMode) {

    public AcademicGpaProperties {
        if (roundingMode == null) {
            throw new IllegalArgumentException("app.academics.gpa.rounding-mode is required.");
        }
    }
}

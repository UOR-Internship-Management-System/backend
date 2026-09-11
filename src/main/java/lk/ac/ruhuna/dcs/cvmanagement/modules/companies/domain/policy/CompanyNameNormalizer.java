package lk.ac.ruhuna.dcs.cvmanagement.modules.companies.domain.policy;

import java.util.Locale;
import java.util.regex.Pattern;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;

/**
 * Canonical Company-name normalization shared by friendly duplicate checks and persisted display
 * values. PostgreSQL remains the final uniqueness authority through {@code normalized_name}.
 */
public final class CompanyNameNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+", Pattern.UNICODE_CHARACTER_CLASS);

    private CompanyNameNormalizer() {
    }

    public static String displayName(String rawName) {
        if (rawName == null) {
            throw new ValidationException("Company name is required.");
        }
        String normalized = WHITESPACE.matcher(rawName.strip()).replaceAll(" ");
        if (normalized.isEmpty()) {
            throw new ValidationException("Company name is required.");
        }
        if (normalized.codePointCount(0, normalized.length()) > 200) {
            throw new ValidationException("Company name must not exceed 200 characters.");
        }
        return normalized;
    }

    public static String duplicateKey(String displayName) {
        return displayName(displayName).toLowerCase(Locale.ROOT);
    }
}

package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain;

import java.util.Locale;
import java.util.Optional;

/** Closed official-result vocabulary used by Academic Ledger validation and persistence. */
public enum AcademicResultStatus {
    PASSED,
    FAILED,
    ABSENT;

    public static Optional<AcademicResultStatus> fromExternalValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public static AcademicResultStatus expectedFor(String gradeCode, boolean passing) {
        if ("E*".equalsIgnoreCase(gradeCode)) {
            return ABSENT;
        }
        return passing ? PASSED : FAILED;
    }
}

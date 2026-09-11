package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Pure validation helpers shared by the bounded validation batches. */
final class AcademicLedgerValidationRules {

    private static final Pattern STUDENT_INDEX = Pattern.compile("^[A-Z]{2}/([0-9]{4})/[0-9]{5}$");

    private AcademicLedgerValidationRules() {
    }

    static Short cohortYear(String indexNumber) {
        if (indexNumber == null) {
            return null;
        }
        Matcher matcher = STUDENT_INDEX.matcher(indexNumber.toUpperCase(Locale.ROOT));
        if (!matcher.matches()) {
            return null;
        }
        return Short.valueOf(matcher.group(1));
    }

    static boolean sameCredits(BigDecimal uploaded, BigDecimal canonical) {
        return uploaded != null && canonical != null && uploaded.compareTo(canonical) == 0;
    }

    static boolean sameSemester(String uploaded, String canonical) {
        if (uploaded == null || canonical == null) {
            return false;
        }
        return AcademicLedgerSourceParser.normalizeSemester(uploaded)
                .equalsIgnoreCase(AcademicLedgerSourceParser.normalizeSemester(canonical));
    }

    static String sanitizeRejectedValue(String value) {
        if (value == null) {
            return null;
        }
        String sanitized = value.replaceAll("[\\p{Cntrl}&&[^\\t]]", "?").trim();
        return sanitized.length() <= 120 ? sanitized : sanitized.substring(0, 120);
    }
}

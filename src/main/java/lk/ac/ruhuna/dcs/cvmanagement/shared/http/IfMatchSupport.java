package lk.ac.ruhuna.dcs.cvmanagement.shared.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.PreconditionRequiredException;

/** Helpers for the quoted integer ETag/If-Match contract used by mutable resources. */
public final class IfMatchSupport {

    private static final Pattern QUOTED_NON_NEGATIVE_VERSION = Pattern.compile("^\"([0-9]+)\"$");

    private IfMatchSupport() {
    }

    /**
     * Parses a strong quoted version such as {@code "3"}.
     *
     * <p>Missing headers are a 428 precondition failure. Weak ETags, wildcards, unquoted values,
     * negative values and non-numeric values are rejected as malformed requests.
     */
    public static long parseVersion(String ifMatchHeader) {
        if (ifMatchHeader == null || ifMatchHeader.isBlank()) {
            throw new PreconditionRequiredException("If-Match header is required for this operation.");
        }

        Matcher matcher = QUOTED_NON_NEGATIVE_VERSION.matcher(ifMatchHeader.trim());
        if (!matcher.matches()) {
            throw new BadRequestException(
                    "If-Match header must be a quoted non-negative integer entity version, for example \"3\".");
        }

        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw new BadRequestException("If-Match entity version is outside the supported numeric range.");
        }
    }

    /** Formats a non-negative entity version as a strong quoted ETag value. */
    public static String formatVersion(long version) {
        if (version < 0) {
            throw new IllegalArgumentException("Entity version must be non-negative.");
        }
        return '"' + Long.toString(version) + '"';
    }
}

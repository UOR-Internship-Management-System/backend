package lk.ac.ruhuna.dcs.cvmanagement.shared.http;

import lk.ac.ruhuna.dcs.cvmanagement.shared.error.BadRequestException;

public final class IfMatchSupport {

    private IfMatchSupport() {
    }

    /** Parses a quoted version string like "3" into 3L. Frontend sends this exact format. */
    public static long parseVersion(String ifMatchHeader) {
        if (ifMatchHeader == null || ifMatchHeader.isBlank()) {
            throw new BadRequestException("If-Match header is required for this operation.");
        }
        String trimmed = ifMatchHeader.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            throw new BadRequestException("If-Match header must contain a numeric entity version.");
        }
    }
}

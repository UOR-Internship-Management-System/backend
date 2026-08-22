package lk.ac.ruhuna.dcs.cvmanagement.modules.auditlog.application;

import java.lang.reflect.Array;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuditMetadataPolicy {

    private static final int MAX_DEPTH = 4;
    private static final int MAX_ENTRIES = 32;
    private static final int MAX_COLLECTION_SIZE = 50;
    private static final int MAX_KEY_LENGTH = 64;
    private static final int MAX_STRING_LENGTH = 512;
    private static final Set<String> FORBIDDEN_KEY_PARTS = Set.of(
            "password", "passphrase", "otp", "secret", "token", "authorization", "cookie",
            "credential", "privatekey", "resetcode", "filecontent", "sql", "stacktrace", "storagepath");

    public void validate(Map<String, ?> metadata) {
        validateMap(metadata == null ? Map.of() : metadata, 0, new Counter());
    }

    private void validateMap(Map<?, ?> values, int depth, Counter counter) {
        requireDepth(depth);
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw invalid("Audit metadata keys must be strings.");
            }
            if (key.isBlank() || key.length() > MAX_KEY_LENGTH) {
                throw invalid("Audit metadata contains an invalid key.");
            }
            String normalizedKey = key.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
            if (FORBIDDEN_KEY_PARTS.stream().anyMatch(normalizedKey::contains)) {
                throw invalid("Audit metadata contains a forbidden key.");
            }
            counter.increment();
            validateValue(entry.getValue(), depth + 1, counter);
        }
    }

    private void validateValue(Object value, int depth, Counter counter) {
        if (value == null || value instanceof Number || value instanceof Boolean || value instanceof UUID
                || value instanceof Enum<?> || value instanceof TemporalAccessor) {
            return;
        }
        if (value instanceof CharSequence text) {
            if (text.length() > MAX_STRING_LENGTH) {
                throw invalid("Audit metadata contains an oversized string value.");
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            validateMap(map, depth, counter);
            return;
        }
        if (value instanceof Collection<?> collection) {
            requireDepth(depth);
            if (collection.size() > MAX_COLLECTION_SIZE) {
                throw invalid("Audit metadata contains an oversized collection.");
            }
            collection.forEach(item -> validateValue(item, depth + 1, counter));
            return;
        }
        if (value.getClass().isArray()) {
            requireDepth(depth);
            int length = Array.getLength(value);
            if (length > MAX_COLLECTION_SIZE) {
                throw invalid("Audit metadata contains an oversized array.");
            }
            for (int index = 0; index < length; index++) {
                validateValue(Array.get(value, index), depth + 1, counter);
            }
            return;
        }
        throw invalid("Audit metadata contains an unsupported value type.");
    }

    private void requireDepth(int depth) {
        if (depth > MAX_DEPTH) {
            throw invalid("Audit metadata nesting is too deep.");
        }
    }

    private IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }

    private static final class Counter {
        private int entries;

        private void increment() {
            entries++;
            if (entries > MAX_ENTRIES) {
                throw new IllegalArgumentException("Audit metadata contains too many entries.");
            }
        }
    }
}


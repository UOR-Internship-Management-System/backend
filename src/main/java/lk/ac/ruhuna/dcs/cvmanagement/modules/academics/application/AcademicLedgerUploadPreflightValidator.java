package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Performs the synchronous, transport-level preflight required before an upload may receive 202.
 *
 * <p>This class intentionally validates only the frozen file/CSV contract and primitive parseability.
 * Student, subject, grade-scale, duplicate-row, and official-record rules belong to asynchronous
 * domain validation in Patch 3.
 */
@Component
public class AcademicLedgerUploadPreflightValidator {

    private static final String CSV_MEDIA_TYPE = "text/csv";
    private static final Pattern ACADEMIC_YEAR = Pattern.compile("^[0-9]{4}/[0-9]{4}$");
    private static final int COLUMN_COUNT = 8;

    private static final int STUDENT_INDEX = 0;
    private static final int COURSE_CODE = 1;
    private static final int CREDITS = 2;
    private static final int LETTER_GRADE = 3;
    private static final int SEMESTER = 4;
    private static final int ACADEMIC_YEAR_INDEX = 5;
    private static final int ATTEMPT_NUMBER = 6;
    private static final int RESULT_STATUS = 7;

    private final AcademicLedgerProperties properties;

    public AcademicLedgerUploadPreflightValidator(AcademicLedgerProperties properties) {
        this.properties = properties;
    }

    public ValidatedLedgerFile validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() <= 0) {
            throw AcademicLedgerErrors.badRequest("A non-empty multipart field named 'file' is required.");
        }
        if (file.getSize() > properties.maxFileSizeBytes()) {
            throw AcademicLedgerErrors.fileTooLarge(properties.maxFileSizeBytes());
        }

        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        validateFileIdentity(originalFilename, file.getContentType());

        String checksum = parseAndHash(file);
        return new ValidatedLedgerFile(originalFilename, CSV_MEDIA_TYPE, file.getSize(), checksum);
    }

    private String parseAndHash(MultipartFile file) {
        MessageDigest digest = sha256();
        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        CSVFormat format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(false)
                .setAllowMissingColumnNames(false)
                .get();

        try (DigestInputStream digestInput = new DigestInputStream(
                        new BufferedInputStream(file.getInputStream()), digest);
                InputStreamReader reader = new InputStreamReader(digestInput, decoder);
                CSVParser parser = format.parse(reader)) {
            validateHeaders(parser.getHeaderNames());
            for (CSVRecord record : parser) {
                validateRecord(record);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (AcademicLedgerApiException exception) {
            throw exception;
        } catch (IOException | UncheckedIOException | IllegalArgumentException exception) {
            throw AcademicLedgerErrors.parseFailed();
        }
    }

    private void validateHeaders(List<String> actualHeaders) {
        if (!AcademicLedgerErrors.expectedHeaders().equals(actualHeaders)) {
            throw AcademicLedgerErrors.parseFailed();
        }
    }

    private void validateRecord(CSVRecord record) {
        if (record.size() != COLUMN_COUNT) {
            throw AcademicLedgerErrors.parseFailed();
        }

        requireText(record.get(STUDENT_INDEX), 40);
        requireText(record.get(COURSE_CODE), 30);
        validateCredits(record.get(CREDITS));
        requireText(record.get(LETTER_GRADE), 5);
        requireText(record.get(SEMESTER), 80);
        validateAcademicYear(record.get(ACADEMIC_YEAR_INDEX));
        validateAttemptNumber(record.get(ATTEMPT_NUMBER));
        requireText(record.get(RESULT_STATUS), 30);
    }

    private void validateCredits(String rawValue) {
        String value = requireText(rawValue, 16);
        try {
            BigDecimal credits = new BigDecimal(value);
            if (credits.compareTo(BigDecimal.ZERO) <= 0
                    || credits.compareTo(new BigDecimal("30.0")) > 0
                    || credits.stripTrailingZeros().scale() > 1) {
                throw AcademicLedgerErrors.parseFailed();
            }
            // Verify it fits the database precision without allowing implicit numeric rounding.
            credits.setScale(1, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException | ArithmeticException exception) {
            throw AcademicLedgerErrors.parseFailed();
        }
    }

    private void validateAcademicYear(String rawValue) {
        String value = requireText(rawValue, 9);
        if (!ACADEMIC_YEAR.matcher(value).matches()) {
            throw AcademicLedgerErrors.parseFailed();
        }
    }

    private void validateAttemptNumber(String rawValue) {
        String value = requireText(rawValue, 2);
        try {
            int attempt = Integer.parseInt(value);
            if (attempt < 1 || attempt > 20) {
                throw AcademicLedgerErrors.parseFailed();
            }
        } catch (NumberFormatException exception) {
            throw AcademicLedgerErrors.parseFailed();
        }
    }

    private String requireText(String rawValue, int maxLength) {
        if (rawValue == null) {
            throw AcademicLedgerErrors.parseFailed();
        }
        String value = rawValue.trim();
        if (value.isEmpty() || value.length() > maxLength || value.indexOf('\0') >= 0) {
            throw AcademicLedgerErrors.parseFailed();
        }
        return value;
    }

    private void validateFileIdentity(String filename, String contentType) {
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
            throw AcademicLedgerErrors.unsupportedMediaType();
        }
        try {
            MediaType mediaType = contentType == null ? null : MediaType.parseMediaType(contentType);
            if (mediaType == null
                    || !"text".equalsIgnoreCase(mediaType.getType())
                    || !"csv".equalsIgnoreCase(mediaType.getSubtype())
                    || (mediaType.getCharset() != null && !StandardCharsets.UTF_8.equals(mediaType.getCharset()))) {
                throw AcademicLedgerErrors.unsupportedMediaType();
            }
        } catch (InvalidMediaTypeException exception) {
            throw AcademicLedgerErrors.unsupportedMediaType();
        }
    }

    private String sanitizeFilename(String suppliedFilename) {
        if (suppliedFilename == null) {
            throw AcademicLedgerErrors.badRequest("The uploaded file must have a filename.");
        }
        String normalized = suppliedFilename.replace('\\', '/');
        int separator = normalized.lastIndexOf('/');
        String filename = separator >= 0 ? normalized.substring(separator + 1) : normalized;
        filename = filename.trim();
        if (filename.isEmpty() || filename.length() > 255 || filename.indexOf('\0') >= 0) {
            throw AcademicLedgerErrors.badRequest("The uploaded filename is invalid.");
        }
        return filename;
    }

    private MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available in this JVM.", exception);
        }
    }

    public record ValidatedLedgerFile(
            String originalFilename,
            String contentType,
            long sizeBytes,
            String checksumSha256) {
    }
}

package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.function.Consumer;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerErrors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

/**
 * Replays the persisted source CSV defensively and emits bounded normalized batches.
 *
 * <p>Patch 2 already performs synchronous preflight. Re-validating the persisted source here is
 * intentional: a source file is an external dependency and must not be trusted to remain intact
 * between acceptance and asynchronous processing.
 */
@Component
class AcademicLedgerSourceParser {

    private static final List<String> HEADERS = AcademicLedgerErrors.expectedHeaders();
    private static final int COLUMN_COUNT = 8;
    private static final Pattern ACADEMIC_YEAR = Pattern.compile("^[0-9]{4}/[0-9]{4}$");

    private final ObjectMapper objectMapper;

    AcademicLedgerSourceParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    int parse(InputStream input, int batchSize, Consumer<List<AcademicLedgerParsedRow>> batchConsumer) {
        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        CSVFormat format = CSVFormat.RFC4180.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(false)
                .setAllowMissingColumnNames(false)
                .get();

        int totalRows = 0;
        List<AcademicLedgerParsedRow> batch = new ArrayList<>(batchSize);
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(input), decoder);
                CSVParser parser = format.parse(reader)) {
            if (!HEADERS.equals(parser.getHeaderNames())) {
                throw new AcademicLedgerProcessingException("Persisted CSV header no longer matches the accepted contract.");
            }
            int physicalRowNumber = 2;
            for (CSVRecord record : parser) {
                AcademicLedgerParsedRow row = parseRecord(record, physicalRowNumber++);
                batch.add(row);
                totalRows++;
                if (batch.size() == batchSize) {
                    batchConsumer.accept(List.copyOf(batch));
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                batchConsumer.accept(List.copyOf(batch));
            }
            return totalRows;
        } catch (AcademicLedgerProcessingException exception) {
            throw exception;
        } catch (IOException | UncheckedIOException | IllegalArgumentException exception) {
            throw new AcademicLedgerProcessingException("Persisted Academic Ledger CSV could not be parsed.", exception);
        }
    }

    private AcademicLedgerParsedRow parseRecord(CSVRecord record, int physicalRowNumber) {
        if (record.size() != COLUMN_COUNT) {
            throw new AcademicLedgerProcessingException("Persisted CSV row has an invalid column count.");
        }
        String studentIndex = required(record.get(0), 40).toUpperCase(Locale.ROOT);
        String courseCode = required(record.get(1), 30).toUpperCase(Locale.ROOT);
        BigDecimal credits = decimal(record.get(2));
        String letterGrade = required(record.get(3), 5).toUpperCase(Locale.ROOT);
        String semester = normalizeSemester(required(record.get(4), 80));
        String academicYear = academicYear(record.get(5));
        short attemptNumber = attemptNumber(record.get(6));
        String resultStatus = required(record.get(7), 30).toUpperCase(Locale.ROOT);

        ObjectNode raw = objectMapper.createObjectNode();
        for (int index = 0; index < HEADERS.size(); index++) {
            raw.put(HEADERS.get(index), record.get(index));
        }
        return new AcademicLedgerParsedRow(
                physicalRowNumber,
                raw,
                studentIndex,
                courseCode,
                credits,
                letterGrade,
                semester,
                academicYear,
                attemptNumber,
                resultStatus);
    }

    private BigDecimal decimal(String raw) {
        try {
            BigDecimal value = new BigDecimal(required(raw, 16)).setScale(1, RoundingMode.UNNECESSARY);
            if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(new BigDecimal("30.0")) > 0) {
                throw new AcademicLedgerProcessingException("Persisted CSV contains out-of-range credits.");
            }
            return value;
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new AcademicLedgerProcessingException("Persisted CSV contains invalid credits.", exception);
        }
    }


    private String academicYear(String raw) {
        String value = required(raw, 9);
        if (!ACADEMIC_YEAR.matcher(value).matches()) {
            throw new AcademicLedgerProcessingException("Persisted CSV contains an invalid academic year.");
        }
        return value;
    }

    private short attemptNumber(String raw) {
        try {
            short value = Short.parseShort(required(raw, 2));
            if (value < 1 || value > 20) {
                throw new AcademicLedgerProcessingException("Persisted CSV contains an out-of-range attempt number.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new AcademicLedgerProcessingException("Persisted CSV contains an invalid attempt number.", exception);
        }
    }

    static String normalizeSemester(String raw) {
        String value = raw.trim();
        String compact = value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return switch (compact) {
            case "semester 1", "semester i" -> "Semester 1";
            case "semester 2", "semester ii" -> "Semester 2";
            default -> value;
        };
    }

    private String required(String raw, int maxLength) {
        if (raw == null) {
            throw new AcademicLedgerProcessingException("Persisted CSV contains a missing required value.");
        }
        String value = raw.trim();
        if (value.isEmpty() || value.length() > maxLength || value.indexOf('\0') >= 0) {
            throw new AcademicLedgerProcessingException("Persisted CSV contains an invalid required value.");
        }
        return value;
    }
}

package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
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
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

/**
 * Replays the persisted source file (CSV or Excel) defensively and emits bounded normalized batches.
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

    int parse(
            InputStream input,
            String mimeType,
            int batchSize,
            Consumer<List<AcademicLedgerParsedRow>> batchConsumer) {
        if (AcademicLedgerErrors.XLSX_MEDIA_TYPE.equals(mimeType)) {
            return parseExcel(input, batchSize, batchConsumer);
        }
        return parseCsv(input, batchSize, batchConsumer);
    }

    private int parseCsv(
            InputStream input, int batchSize, Consumer<List<AcademicLedgerParsedRow>> batchConsumer) {
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
                throw new AcademicLedgerProcessingException("Persisted source header no longer matches the accepted contract.");
            }
            int physicalRowNumber = 2;
            for (CSVRecord record : parser) {
                AcademicLedgerParsedRow row = parseRecord(new CsvLedgerRow(record), physicalRowNumber++);
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

    private int parseExcel(
            InputStream input, int batchSize, Consumer<List<AcademicLedgerParsedRow>> batchConsumer) {
        byte[] bytes;
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            input.transferTo(buffer);
            bytes = buffer.toByteArray();
        } catch (IOException exception) {
            throw new AcademicLedgerProcessingException("Persisted Academic Ledger Excel file could not be read.", exception);
        }

        DataFormatter formatter = new DataFormatter(Locale.ENGLISH);
        try (Workbook workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(bytes))) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new AcademicLedgerProcessingException("Persisted Academic Ledger Excel file has no sheets.");
            }
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (!HEADERS.equals(readHeaderRow(headerRow, formatter))) {
                throw new AcademicLedgerProcessingException("Persisted source header no longer matches the accepted contract.");
            }

            int totalRows = 0;
            int physicalRowNumber = 2;
            List<AcademicLedgerParsedRow> batch = new ArrayList<>(batchSize);
            int lastRow = sheet.getLastRowNum();
            for (int rowIndex = sheet.getFirstRowNum() + 1; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row)) {
                    continue;
                }
                int size = Math.max(row.getLastCellNum(), 0);
                AcademicLedgerParsedRow parsed = parseRecord(
                        new ExcelLedgerRow(row, size, formatter), physicalRowNumber++);
                batch.add(parsed);
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
        } catch (Exception exception) {
            throw new AcademicLedgerProcessingException("Persisted Academic Ledger Excel file could not be parsed.", exception);
        }
    }

    private List<String> readHeaderRow(Row headerRow, DataFormatter formatter) {
        if (headerRow == null) {
            return List.of();
        }
        List<String> headers = new ArrayList<>();
        int lastCell = headerRow.getLastCellNum();
        ExcelLedgerRow accessor = new ExcelLedgerRow(headerRow, lastCell, formatter);
        for (int index = 0; index < lastCell; index++) {
            headers.add(accessor.get(index));
        }
        return headers;
    }

    private boolean isBlankRow(Row row) {
        for (int index = 0; index < row.getLastCellNum(); index++) {
            var cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                return false;
            }
        }
        return true;
    }

    private AcademicLedgerParsedRow parseRecord(LedgerRow record, int physicalRowNumber) {
        if (record.size() != COLUMN_COUNT) {
            throw new AcademicLedgerProcessingException("Persisted source row has an invalid column count.");
        }
        String studentIndex = required(record.get(0), 40).toUpperCase(Locale.ROOT);
        String courseCode = normalizeCourseCode(required(record.get(1), 30));
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
                throw new AcademicLedgerProcessingException("Persisted source contains out-of-range credits.");
            }
            return value;
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new AcademicLedgerProcessingException("Persisted source contains invalid credits.", exception);
        }
    }


    private String academicYear(String raw) {
        String value = required(raw, 9);
        if (!ACADEMIC_YEAR.matcher(value).matches()) {
            throw new AcademicLedgerProcessingException("Persisted source contains an invalid academic year.");
        }
        return value;
    }

    private short attemptNumber(String raw) {
        try {
            short value = Short.parseShort(required(raw, 2));
            if (value < 1 || value > 20) {
                throw new AcademicLedgerProcessingException("Persisted source contains an out-of-range attempt number.");
            }
            return value;
        } catch (NumberFormatException exception) {
            throw new AcademicLedgerProcessingException("Persisted source contains an invalid attempt number.", exception);
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

    static String normalizeCourseCode(String raw) {
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return normalized
                .replace('Α', 'α')
                .replace('Β', 'β')
                .replace('Δ', 'δ');
    }

    private String required(String raw, int maxLength) {
        if (raw == null) {
            throw new AcademicLedgerProcessingException("Persisted source contains a missing required value.");
        }
        String value = raw.trim();
        if (value.isEmpty() || value.length() > maxLength || value.indexOf('\0') >= 0) {
            throw new AcademicLedgerProcessingException("Persisted source contains an invalid required value.");
        }
        return value;
    }

    private record CsvLedgerRow(CSVRecord record) implements LedgerRow {
        @Override
        public String get(int index) {
            return record.get(index);
        }

        @Override
        public int size() {
            return record.size();
        }
    }
}

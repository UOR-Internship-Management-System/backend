package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Parses an eligible-student bulk-upload file (.csv, .xlsx, or .xls) into raw, unvalidated rows. */
@Component
public class EligibleStudentFileParser {

    /** One spreadsheet/CSV data row, 1-based excluding the header (row 1 is the first data row). */
    public record ParsedRow(
        int row, String indexNumber, String universityEmail, String fullName, String academicLevel) {
    }

    public List<ParsedRow> parse(MultipartFile file) {
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        try {
            if (filename.endsWith(".csv")) {
                return parseCsv(file.getInputStream());
            }
            if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
                return parseExcel(file.getInputStream());
            }
        } catch (IOException exception) {
            throw new ValidationException("The uploaded file could not be read.");
        }
        throw new ValidationException("Only .csv, .xlsx, or .xls files are supported.");
    }

    private List<ParsedRow> parseCsv(InputStream input) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .get();
        List<ParsedRow> rows = new ArrayList<>();
        try (var parser = format.parse(new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8))) {
            Map<String, Integer> columns = normalizeHeaders(parser.getHeaderNames());
            int rowNumber = 0;
            for (CSVRecord record : parser) {
                rowNumber++;
                rows.add(new ParsedRow(
                    rowNumber,
                    valueOf(record, columns, "indexnumber"),
                    valueOf(record, columns, "universityemail"),
                    valueOf(record, columns, "fullname"),
                    valueOf(record, columns, "academiclevel")));
            }
        }
        return rows;
    }

    private String valueOf(CSVRecord record, Map<String, Integer> columns, String key) {
        Integer index = columns.get(key);
        if (index == null || index >= record.size()) return "";
        return record.get(index).strip();
    }

    private List<ParsedRow> parseExcel(InputStream input) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) return rows;
            Map<String, Integer> columns = new LinkedHashMap<>();
            for (Cell cell : headerRow) {
                String header = normalizeHeader(cellText(cell));
                if (!header.isEmpty()) columns.put(header, cell.getColumnIndex());
            }
            int rowNumber = 0;
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row dataRow = sheet.getRow(r);
                if (dataRow == null || isBlankRow(dataRow)) continue;
                rowNumber++;
                rows.add(new ParsedRow(
                    rowNumber,
                    excelValue(dataRow, columns, "indexnumber"),
                    excelValue(dataRow, columns, "universityemail"),
                    excelValue(dataRow, columns, "fullname"),
                    excelValue(dataRow, columns, "academiclevel")));
            }
        }
        return rows;
    }

    private boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (!cellText(cell).isBlank()) return false;
        }
        return true;
    }

    private String excelValue(Row row, Map<String, Integer> columns, String key) {
        Integer index = columns.get(key);
        if (index == null) return "";
        Cell cell = row.getCell(index);
        return cell == null ? "" : cellText(cell).strip();
    }

    private String cellText(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (value == Math.floor(value) && !Double.isInfinite(value)) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return cell.getStringCellValue();
        }
        return cell.toString();
    }

    private Map<String, Integer> normalizeHeaders(List<String> headers) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = normalizeHeader(headers.get(i));
            if (!header.isEmpty()) columns.put(header, i);
        }
        return columns;
    }

    private String normalizeHeader(String value) {
        if (value == null) return "";
        String normalized = value.strip().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        return switch (normalized) {
            case "indexnumber", "index" -> "indexnumber";
            case "universityemail", "email", "universitymail" -> "universityemail";
            case "fullname", "name", "studentname" -> "fullname";
            case "academiclevel", "level", "year", "academicyear" -> "academiclevel";
            default -> normalized;
        };
    }
}

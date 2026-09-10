package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application;

import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

/** Adapts a POI spreadsheet row to {@link LedgerRow}, normalizing numeric cells to plain decimal text. */
final class ExcelLedgerRow implements LedgerRow {

    private final Row row;
    private final int size;
    private final DataFormatter formatter;

    ExcelLedgerRow(Row row, int size, DataFormatter formatter) {
        this.row = row;
        this.size = size;
        this.formatter = formatter;
    }

    @Override
    public String get(int index) {
        Cell cell = row == null ? null : row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
        }
        return formatter.formatCellValue(cell).trim();
    }

    @Override
    public int size() {
        return size;
    }
}

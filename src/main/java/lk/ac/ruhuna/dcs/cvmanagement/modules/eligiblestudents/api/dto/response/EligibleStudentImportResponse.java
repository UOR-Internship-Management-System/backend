package lk.ac.ruhuna.dcs.cvmanagement.modules.eligiblestudents.api.dto.response;

import java.util.List;

public record EligibleStudentImportResponse(
    int totalRows,
    int importedCount,
    int skippedCount,
    List<RowError> errors) {

    public record RowError(int row, String message) {
    }
}

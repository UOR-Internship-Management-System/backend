package lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

/** Factory methods for stable Academic Ledger error contracts. */
public final class AcademicLedgerErrors {

    private static final List<String> EXPECTED_HEADERS = List.of(
            "student_index_number",
            "course_code",
            "credits",
            "letter_grade",
            "semester",
            "academic_year",
            "attempt_number",
            "result_status");

    private AcademicLedgerErrors() {
    }

    public static AcademicLedgerApiException fileTooLarge(long maxSizeBytes) {
        return new AcademicLedgerApiException(
                HttpStatus.CONTENT_TOO_LARGE,
                "LEDGER_FILE_TOO_LARGE",
                "Academic ledger file is too large",
                "Upload a CSV file no larger than 5 MiB.",
                Map.of("maxSizeBytes", maxSizeBytes));
    }

    public static AcademicLedgerApiException unsupportedMediaType() {
        return new AcademicLedgerApiException(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "LEDGER_MEDIA_TYPE_UNSUPPORTED",
                "Unsupported academic ledger format",
                "Upload a UTF-8 CSV file with media type text/csv.",
                Map.of("acceptedMediaType", "text/csv", "acceptedExtension", ".csv"));
    }

    public static AcademicLedgerApiException parseFailed() {
        return new AcademicLedgerApiException(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "LEDGER_PARSE_FAILED",
                "Academic ledger could not be parsed",
                "The CSV header or one or more rows do not match the approved academic-ledger format.",
                Map.of("expectedHeaders", EXPECTED_HEADERS));
    }

    public static AcademicLedgerApiException duplicateUpload(UUID existingUploadId) {
        return new AcademicLedgerApiException(
                HttpStatus.CONFLICT,
                "LEDGER_DUPLICATE_UPLOAD",
                "Duplicate academic ledger upload",
                "Identical academic ledger content already exists in an active or committed batch.",
                Map.of("existingUploadId", existingUploadId.toString()));
    }

    public static AcademicLedgerApiException uploadNotFound() {
        return new AcademicLedgerApiException(
                HttpStatus.NOT_FOUND,
                "LEDGER_UPLOAD_NOT_FOUND",
                "Academic ledger upload not found",
                "No academic ledger upload exists for the supplied identifier.",
                Map.of());
    }

    public static AcademicLedgerApiException unauthorized() {
        return new AcademicLedgerApiException(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication required",
                "Authentication is required to access this resource.",
                Map.of());
    }

    public static AcademicLedgerApiException forbidden() {
        return new AcademicLedgerApiException(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Access denied",
                "The current account cannot access this resource.",
                Map.of());
    }

    public static AcademicLedgerApiException badRequest(String message) {
        return new AcademicLedgerApiException(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Invalid academic ledger request",
                message,
                Map.of());
    }

    public static AcademicLedgerApiException notReady(String message) {
        return new AcademicLedgerApiException(
                HttpStatus.CONFLICT,
                "LEDGER_NOT_READY",
                "Academic ledger is not ready",
                message,
                Map.of());
    }

    public static AcademicLedgerApiException notReadyToCommit(String currentStatus) {
        return new AcademicLedgerApiException(
                HttpStatus.CONFLICT,
                "LEDGER_NOT_READY_TO_COMMIT",
                "Academic ledger is not ready to commit",
                "Only a READY_TO_COMMIT upload batch can be committed.",
                Map.of("currentStatus", currentStatus));
    }

    public static AcademicLedgerApiException alreadyCommitted() {
        return new AcademicLedgerApiException(
                HttpStatus.CONFLICT,
                "LEDGER_ALREADY_COMMITTED",
                "Academic ledger is already committed",
                "The upload batch has already been committed and cannot be committed again.",
                Map.of("currentStatus", "COMMITTED"));
    }

    public static AcademicLedgerApiException commitConflict() {
        return new AcademicLedgerApiException(
                HttpStatus.CONFLICT,
                "LEDGER_COMMIT_CONFLICT",
                "Academic ledger commit conflict",
                "Another request is currently committing this upload batch.",
                Map.of("currentStatus", "COMMITTING"));
    }

    public static AcademicLedgerApiException validationFailed(int invalidRows) {
        return new AcademicLedgerApiException(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "LEDGER_VALIDATION_FAILED",
                "Academic ledger validation failed",
                "Resolve the invalid rows in the source file and upload a corrected batch.",
                Map.of("invalidRows", Math.max(invalidRows, 0)));
    }

    public static AcademicLedgerApiException commitFailed() {
        return new AcademicLedgerApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "LEDGER_COMMIT_FAILED",
                "Academic ledger commit failed",
                "No academic records were committed. The batch remains ready for a safe retry.",
                Map.of("transactionRolledBack", true));
    }

    public static AcademicLedgerApiException storageUnavailable() {
        return new AcademicLedgerApiException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "DEPENDENCY_UNAVAILABLE",
                "Academic ledger storage is unavailable",
                "The academic ledger file could not be stored. Try again later.",
                Map.of());
    }

    public static AcademicLedgerApiException internalFailure() {
        return new AcademicLedgerApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Academic ledger operation failed",
                "The academic ledger request could not be completed.",
                Map.of());
    }

    public static List<String> expectedHeaders() {
        return EXPECTED_HEADERS;
    }
}

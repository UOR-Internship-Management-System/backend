package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

/**
 * Stable machine-readable error codes exposed by the shared Problem Details contract.
 *
 * <p>Feature-specific codes live here only when they are part of the public OpenAPI contract and
 * are shared by controller/advice infrastructure. This keeps clients from depending on exception
 * class names or framework-specific messages.
 */
public enum ApiErrorCode {
    BAD_REQUEST,
    VALIDATION_FAILED,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    CONFLICT,
    PRECONDITION_FAILED,
    IF_MATCH_REQUIRED,
    PRECONDITION_REQUIRED,
    STALE_VERSION,
    CV_CONFIGURATION_INVALID,
    CV_GENERATION_FAILED,
    CV_PREVIEW_EXPIRED,
    CV_NOT_SAVED,
    CV_FILE_UNAVAILABLE,
    UNSUPPORTED_MEDIA_TYPE,
    DUPLICATE_COMPANY,
    COMPANY_NOT_FOUND,
    INTERNSHIP_REQUEST_NOT_FOUND,
    DUPLICATE_REQUIRED_SKILL,
    INVALID_TAXONOMY_SKILL,
    DEPENDENCY_UNAVAILABLE,
    INTERNAL_ERROR
}

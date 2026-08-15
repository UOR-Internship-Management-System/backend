package lk.ac.ruhuna.dcs.cvmanagement.shared.error;

/** OpenAPI-compatible validation error for one request field. */
public record ApiFieldError(String field, String code, String message) {
}

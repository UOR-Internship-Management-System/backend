package lk.ac.ruhuna.dcs.cvmanagement.shared.files;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Maps upload validation failures to the RFC 7807 contract the frontend already parses.
 *
 * <p>Status codes match the MSW handlers the UI was built against: 415 for a rejected type, 413 for
 * an oversized file, 422 for a missing multipart part.
 */
@RestControllerAdvice(assignableTypes = {FileContentController.class})
public class ProfileFileExceptionHandler {

    @ExceptionHandler(ProfileFileService.UnsupportedUploadTypeException.class)
    ProblemDetail handleUnsupportedType(
        ProfileFileService.UnsupportedUploadTypeException exception,
        HttpServletRequest request) {
        return problem(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "UNSUPPORTED_MEDIA_TYPE",
            exception.getMessage(),
            request,
            Map.of("permitted", exception.permitted()));
    }

    @ExceptionHandler(ProfileFileService.UploadTooLargeException.class)
    ProblemDetail handleTooLarge(
        ProfileFileService.UploadTooLargeException exception,
        HttpServletRequest request) {
        return problem(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "FILE_TOO_LARGE",
            exception.getMessage(),
            request,
            Map.of("maxSizeBytes", exception.maxSizeBytes()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ProblemDetail handleContainerLimit(HttpServletRequest request) {
        return problem(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "FILE_TOO_LARGE",
            "The selected file is too large.",
            request,
            Map.of());
    }

    @ExceptionHandler(ProfileFileService.UnprocessableUploadException.class)
    ProblemDetail handleUnprocessable(
        ProfileFileService.UnprocessableUploadException exception,
        HttpServletRequest request) {
        return problem(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "VALIDATION_FAILED",
            exception.getMessage(),
            request,
            Map.of());
    }

    private ProblemDetail problem(
        HttpStatus status,
        String code,
        String detail,
        HttpServletRequest request,
        Map<String, Object> extras) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        problemDetail.setProperty("code", code);
        extras.forEach(problemDetail::setProperty);
        return problemDetail;
    }
}

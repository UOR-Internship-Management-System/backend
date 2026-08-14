package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

/** Factory methods for stable Admin Student Inspection error contracts. */
public final class AdminStudentErrors {

    private AdminStudentErrors() {
    }

    public static AdminStudentApiException unauthorized() {
        return new AdminStudentApiException(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication required",
                "Authentication is required to access this resource.",
                Map.of());
    }

    public static AdminStudentApiException forbidden() {
        return new AdminStudentApiException(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "Access denied",
                "The current account cannot access this resource.",
                Map.of());
    }

    public static AdminStudentApiException badRequest(String message) {
        return new AdminStudentApiException(
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Invalid Admin Student request",
                message,
                Map.of());
    }

    public static RegisteredStudentNotFoundException registeredStudentNotFound() {
        return new RegisteredStudentNotFoundException();
    }

    public static AdminAcademicDataUnavailableException academicDataUnavailable(Throwable cause) {
        return new AdminAcademicDataUnavailableException(cause);
    }

    public static CvNotSavedException cvNotSaved() {
        return new CvNotSavedException();
    }

    public static CvFileUnavailableException cvFileUnavailable(Throwable cause) {
        return new CvFileUnavailableException(cause);
    }
}

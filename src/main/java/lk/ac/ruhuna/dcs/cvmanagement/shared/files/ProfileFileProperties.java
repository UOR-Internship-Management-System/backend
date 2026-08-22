package lk.ac.ruhuna.dcs.cvmanagement.shared.files;

import java.util.List;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Upload constraints and public URL base for student-supplied profile files.
 *
 * <p>{@code publicBaseUrl} must be an absolute {@code http(s)} origin because the frontend renders
 * returned asset URLs directly in {@code <img>} tags and its Zod contract rejects relative paths.
 */
@ConfigurationProperties(prefix = "app.files")
public record ProfileFileProperties(
    String publicBaseUrl,
    Constraint profilePhoto,
    Constraint certificateEvidence) {

    public ProfileFileProperties {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            throw new IllegalArgumentException("app.files.public-base-url must be configured.");
        }
        if (!publicBaseUrl.startsWith("http://") && !publicBaseUrl.startsWith("https://")) {
            throw new IllegalArgumentException(
                "app.files.public-base-url must be an absolute http(s) URL.");
        }
        publicBaseUrl = publicBaseUrl.endsWith("/")
            ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
            : publicBaseUrl;
    }

    public record Constraint(
        List<String> allowedMimeTypes,
        List<String> allowedExtensions,
        long maxSizeBytes) {

        public Constraint {
            if (allowedMimeTypes == null || allowedMimeTypes.isEmpty()) {
                throw new IllegalArgumentException("allowed-mime-types must not be empty.");
            }
            if (allowedExtensions == null || allowedExtensions.isEmpty()) {
                throw new IllegalArgumentException("allowed-extensions must not be empty.");
            }
            if (maxSizeBytes <= 0) {
                throw new IllegalArgumentException("max-size-bytes must be positive.");
            }
            allowedMimeTypes = List.copyOf(allowedMimeTypes);
            allowedExtensions = List.copyOf(allowedExtensions);
        }

        public boolean permitsMimeType(String mimeType) {
            return mimeType != null
                && allowedMimeTypes.contains(mimeType.toLowerCase(Locale.ROOT).trim());
        }

        public boolean permitsExtension(String fileName) {
            if (fileName == null) {
                return false;
            }
            int dot = fileName.lastIndexOf('.');
            if (dot < 0) {
                return false;
            }
            return allowedExtensions.contains(fileName.substring(dot).toLowerCase(Locale.ROOT));
        }
    }
}

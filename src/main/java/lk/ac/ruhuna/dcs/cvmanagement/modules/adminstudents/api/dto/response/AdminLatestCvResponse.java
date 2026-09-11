package lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.api.dto.response;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.CvFreshnessStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.adminstudents.domain.model.LatestCvAvailability;

/** Read-only latest-saved CV metadata matching the OpenAPI v1.6.0 availability contract. */
public record AdminLatestCvResponse(
        LatestCvAvailability availability,
        UUID cvId,
        Integer revision,
        OffsetDateTime generatedAt,
        OffsetDateTime savedAt,
        CvFreshnessStatus freshnessStatus,
        String fileName,
        Long fileSizeBytes,
        String downloadUrl) {

    private static final Pattern SAFE_PDF_NAME = Pattern.compile("^[A-Za-z0-9._-]+\\.pdf$");
    private static final Pattern ADMIN_DOWNLOAD_URL = Pattern.compile(
            "^/admin/students/[0-9a-fA-F-]{36}/latest-cv/download$");

    public AdminLatestCvResponse {
        Objects.requireNonNull(availability, "availability");
        if (availability == LatestCvAvailability.NOT_SAVED) {
            requireAllNull(cvId, revision, generatedAt, savedAt, freshnessStatus, fileName, fileSizeBytes, downloadUrl);
        } else {
            Objects.requireNonNull(cvId, "cvId");
            Objects.requireNonNull(revision, "revision");
            Objects.requireNonNull(generatedAt, "generatedAt");
            Objects.requireNonNull(savedAt, "savedAt");
            Objects.requireNonNull(freshnessStatus, "freshnessStatus");
            Objects.requireNonNull(fileName, "fileName");
            Objects.requireNonNull(fileSizeBytes, "fileSizeBytes");
            Objects.requireNonNull(downloadUrl, "downloadUrl");
            if (revision < 1) {
                throw new IllegalArgumentException("revision must be at least 1");
            }
            if (fileSizeBytes < 1) {
                throw new IllegalArgumentException("fileSizeBytes must be at least 1");
            }
            if (!SAFE_PDF_NAME.matcher(fileName).matches()) {
                throw new IllegalArgumentException("fileName must be a safe PDF filename");
            }
            if (!ADMIN_DOWNLOAD_URL.matcher(downloadUrl).matches()) {
                throw new IllegalArgumentException("downloadUrl must target the Admin latest-CV download route");
            }
        }
    }

    public static AdminLatestCvResponse notSaved() {
        return new AdminLatestCvResponse(
                LatestCvAvailability.NOT_SAVED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public static AdminLatestCvResponse available(
            UUID cvId,
            int revision,
            OffsetDateTime generatedAt,
            OffsetDateTime savedAt,
            CvFreshnessStatus freshnessStatus,
            String fileName,
            long fileSizeBytes,
            UUID studentId) {
        Objects.requireNonNull(studentId, "studentId");
        return new AdminLatestCvResponse(
                LatestCvAvailability.AVAILABLE,
                cvId,
                revision,
                generatedAt,
                savedAt,
                freshnessStatus,
                fileName,
                fileSizeBytes,
                "/admin/students/" + studentId + "/latest-cv/download");
    }

    private static void requireAllNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                throw new IllegalArgumentException("NOT_SAVED CV metadata fields must all be null");
            }
        }
    }
}

package lk.ac.ruhuna.dcs.cvmanagement.modules.studentprofile.api.dto.response;

import java.util.List;

/** Client-facing description of what the server will accept for each upload slot. */
public record ProfileUploadPolicyResponse(
    FileUploadConstraintResponse profilePhoto,
    FileUploadConstraintResponse certificateEvidence) {

    public record FileUploadConstraintResponse(
        List<String> allowedMimeTypes,
        List<String> allowedExtensions,
        long maxSizeBytes) {
    }
}

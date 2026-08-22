package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response;

import java.io.InputStream;

/** Safe streaming download result that never exposes a storage key. */
public record ExportFileResponse(
        String fileName, String contentType, long fileSizeBytes, InputStream content) {}

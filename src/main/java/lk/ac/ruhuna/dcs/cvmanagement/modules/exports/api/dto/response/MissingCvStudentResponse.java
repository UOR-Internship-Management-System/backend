package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response;

import java.util.UUID;

public record MissingCvStudentResponse(UUID studentId, String indexNumber, String fullName) {}

package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.response;

import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportWarningCode;

public record ExportWarningResponse(ExportWarningCode code, String message) {}

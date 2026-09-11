package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportFormat;

public record ShortlistSummaryExportCreateRequest(@NotNull ExportFormat format) {}

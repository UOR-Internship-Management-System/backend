package lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application;

import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;

/** Immutable worker input captured when a queued job is claimed. */
public record ExportJobSnapshot(UUID exportJobId, UUID shortlistId, ExportType exportType) {}

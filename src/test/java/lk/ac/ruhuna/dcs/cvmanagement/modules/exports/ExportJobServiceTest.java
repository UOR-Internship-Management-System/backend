package lk.ac.ruhuna.dcs.cvmanagement.modules.exports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.ExportJobService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.config.ExportProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportFormat;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.domain.policy.ExportType;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.mapper.ExportMapper;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.entity.ExportJobEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.query.ExportReadRepository.ExportShortlist;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository.ExportFileRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository.ExportJobRepository;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.persistence.repository.ExportWarningRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ConflictException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.error.ValidationException;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExportJobServiceTest {
    private final ExportJobRepository jobRepository = mock(ExportJobRepository.class);
    private final ExportFileRepository missingRepository = mock(ExportFileRepository.class);
    private final ExportWarningRepository warningRepository = mock(ExportWarningRepository.class);
    private final ExportReadRepository readRepository = mock(ExportReadRepository.class);
    private final CurrentActorProvider actorProvider = mock(CurrentActorProvider.class);
    private final UUID adminId = UUID.fromString("d3000000-0000-4000-8000-000000000001");
    private final UUID shortlistId = UUID.fromString("d3000000-0000-4000-8000-000000000002");
    private ExportJobService service;

    @BeforeEach
    void setUp() {
        when(actorProvider.currentActor()).thenReturn(Optional.of(
                new CurrentActor(adminId, "admin@dcs.ruh.ac.lk", Set.of(RoleName.ADMIN))));
        when(missingRepository.findAllByExportJobIdOrderByIndexNumberAscStudentIdAsc(any())).thenReturn(List.of());
        when(warningRepository.findAllByExportJobIdOrderByWarningCodeAsc(any())).thenReturn(List.of());
        when(jobRepository.saveAndFlush(any(ExportJobEntity.class))).thenAnswer(invocation -> {
            ExportJobEntity entity = invocation.getArgument(0);
            entity.setVersion(0L);
            return entity;
        });
        service = new ExportJobService(
                jobRepository, missingRepository, warningRepository, readRepository,
                mock(FileAssetRepository.class), mock(FileStoragePort.class), new ExportMapper(),
                actorProvider, mock(AuditEventPublisher.class),
                new ExportProperties(
                        new ExportProperties.Storage(Path.of("data/exports")),
                        new ExportProperties.Processing(false, 2000, 2, Duration.ofDays(7))),
                Clock.fixed(Instant.parse("2026-08-22T03:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void createsQueuedCsvJobForFinalizedShortlist() {
        when(readRepository.findShortlist(shortlistId)).thenReturn(Optional.of(
                new ExportShortlist(shortlistId, true, "Primary", "Acme", "Intern")));

        var response = service.create(shortlistId, ExportType.SHORTLIST_SUMMARY_CSV, ExportFormat.CSV);

        assertThat(response.status()).isEqualTo(ExportStatus.QUEUED);
        assertThat(response.exportType()).isEqualTo(ExportType.SHORTLIST_SUMMARY_CSV);
        assertThat(response.downloadReady()).isFalse();
    }

    @Test
    void rejectsWrongFormatAndDraftShortlist() {
        assertThatThrownBy(() -> service.create(
                        shortlistId, ExportType.SHORTLIST_SUMMARY_CSV, ExportFormat.ZIP))
                .isInstanceOf(ValidationException.class);
        when(readRepository.findShortlist(shortlistId)).thenReturn(Optional.of(
                new ExportShortlist(shortlistId, false, "Primary", "Acme", "Intern")));
        assertThatThrownBy(() -> service.create(
                        shortlistId, ExportType.SHORTLIST_SUMMARY_CSV, ExportFormat.CSV))
                .isInstanceOf(ConflictException.class);
    }
}

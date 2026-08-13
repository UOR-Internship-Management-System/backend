package lk.ac.ruhuna.dcs.cvmanagement.modules.academics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileAssetRepository;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.api.error.AcademicLedgerApiException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerUploadPreflightValidator;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.application.AcademicLedgerUploadService;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.domain.AcademicLedgerUploadStatus;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.entity.AcademicLedgerUploadEntity;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.persistence.repository.AcademicLedgerUploadRepository;
import lk.ac.ruhuna.dcs.cvmanagement.shared.audit.AuditEventPublisher;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActor;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.CurrentActorProvider;
import lk.ac.ruhuna.dcs.cvmanagement.shared.security.RoleName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

class AcademicLedgerUploadServiceTest {

    @Test
    void duplicateActiveContentIsRejectedBeforeWritingAnotherStoredFile() {
        AcademicLedgerUploadPreflightValidator preflight = mock(AcademicLedgerUploadPreflightValidator.class);
        AcademicLedgerUploadRepository uploads = mock(AcademicLedgerUploadRepository.class);
        FileAssetRepository assets = mock(FileAssetRepository.class);
        FileStoragePort storage = mock(FileStoragePort.class);
        CurrentActorProvider actors = mock(CurrentActorProvider.class);
        AuditEventPublisher audit = mock(AuditEventPublisher.class);
        var service = new AcademicLedgerUploadService(
                preflight,
                uploads,
                assets,
                storage,
                new AcademicLedgerProperties(5_242_880L, 2),
                actors,
                audit,
                Clock.systemUTC(),
                noOpTransactionManager());

        UUID adminId = UUID.randomUUID();
        UUID existingUploadId = UUID.randomUUID();
        String checksum = "a".repeat(64);
        MockMultipartFile file = new MockMultipartFile("file", "ledger.csv", "text/csv", "data".getBytes());
        when(actors.currentActor()).thenReturn(Optional.of(
                new CurrentActor(adminId, "admin@example.com", Set.of(RoleName.ADMIN))));
        when(preflight.validate(file)).thenReturn(
                new AcademicLedgerUploadPreflightValidator.ValidatedLedgerFile(
                        "ledger.csv", "text/csv", 4, checksum));
        AcademicLedgerUploadEntity existing = new AcademicLedgerUploadEntity();
        existing.setId(existingUploadId);
        existing.setUploadStatus(AcademicLedgerUploadStatus.RECEIVED);
        when(uploads.findFirstByFileHashAndUploadStatusIn(org.mockito.ArgumentMatchers.eq(checksum), anyCollection()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.upload(file))
                .isInstanceOfSatisfying(AcademicLedgerApiException.class, exception -> {
                    assertThat(exception.status().value()).isEqualTo(409);
                    assertThat(exception.code()).isEqualTo("LEDGER_DUPLICATE_UPLOAD");
                });
        verify(storage, never()).store(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    private PlatformTransactionManager noOpTransactionManager() {
        return new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
            }

            @Override
            public void rollback(TransactionStatus status) {
            }
        };
    }
}

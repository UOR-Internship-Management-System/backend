package lk.ac.ruhuna.dcs.cvmanagement.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.application.port.ActiveCvFileResolver;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvFileUnavailableException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.domain.exception.CvNotSavedException;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.application.port.ExportCvFileLookup;
import org.springframework.stereotype.Component;

/** Infrastructure adapter that keeps the Exports module independent from the CV module. */
@Component
public class ExportCvFileLookupAdapter implements ExportCvFileLookup {
    private final ActiveCvFileResolver activeCvFileResolver;

    public ExportCvFileLookupAdapter(ActiveCvFileResolver activeCvFileResolver) {
        this.activeCvFileResolver = activeCvFileResolver;
    }

    @Override
    public Optional<ExportCvFile> findLatestSaved(UUID studentId) {
        try {
            var resolved = activeCvFileResolver.resolve(studentId);
            return Optional.of(new ExportCvFile(resolved.fileName(), resolved.bytes()));
        } catch (CvNotSavedException | CvFileUnavailableException exception) {
            return Optional.empty();
        }
    }
}

package lk.ac.ruhuna.dcs.cvmanagement.config;

import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.latex.LatexProperties;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.LocalFileStorageAdapter;
import lk.ac.ruhuna.dcs.cvmanagement.modules.cv.config.CvStorageProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** CV-specific infrastructure wiring. The storage root is isolated from Academic Ledger files. */
@Configuration
@EnableConfigurationProperties({CvStorageProperties.class, LatexProperties.class})
public class CvInfrastructureConfig {

    @Bean
    @Qualifier("cvFileStorage")
    public FileStoragePort cvFileStorage(CvStorageProperties properties) {
        return new LocalFileStorageAdapter(properties.root());
    }
}

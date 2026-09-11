package lk.ac.ruhuna.dcs.cvmanagement.config;

import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.LocalFileStorageAdapter;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.StorageProperties;
import lk.ac.ruhuna.dcs.cvmanagement.shared.files.ProfileFileProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Enables strongly typed durable storage for Academic Ledger without sharing its root with CV PDFs. */
@Configuration
@EnableConfigurationProperties({StorageProperties.class, ProfileFileProperties.class})
public class StorageConfig {

    @Bean
    @Primary
    public FileStoragePort academicFileStorage(StorageProperties properties) {
        return new LocalFileStorageAdapter(properties);
    }
}

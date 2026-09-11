package lk.ac.ruhuna.dcs.cvmanagement.config;

import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.FileStoragePort;
import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.LocalFileStorageAdapter;
import lk.ac.ruhuna.dcs.cvmanagement.modules.exports.config.ExportProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Isolated durable storage used only by generated export artifacts. */
@Configuration
@EnableConfigurationProperties(ExportProperties.class)
public class ExportInfrastructureConfig {

    @Bean
    @Qualifier("exportFileStorage")
    public FileStoragePort exportFileStorage(ExportProperties properties) {
        return new LocalFileStorageAdapter(properties.storage().root());
    }
}

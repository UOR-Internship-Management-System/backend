package lk.ac.ruhuna.dcs.cvmanagement.config;

import lk.ac.ruhuna.dcs.cvmanagement.infrastructure.storage.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Enables strongly typed durable-storage configuration. */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {
}

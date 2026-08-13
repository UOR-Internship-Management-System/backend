package lk.ac.ruhuna.dcs.cvmanagement.config;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Enables strongly typed Academic Ledger transport configuration. */
@Configuration
@EnableConfigurationProperties(AcademicLedgerProperties.class)
public class AcademicLedgerConfig {
}

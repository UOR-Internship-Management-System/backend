package lk.ac.ruhuna.dcs.cvmanagement.config;

import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProcessingProperties;
import lk.ac.ruhuna.dcs.cvmanagement.modules.academics.config.AcademicLedgerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables strongly typed Academic Ledger transport, worker, and recovery configuration. */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({AcademicLedgerProperties.class, AcademicLedgerProcessingProperties.class})
public class AcademicLedgerConfig {
}

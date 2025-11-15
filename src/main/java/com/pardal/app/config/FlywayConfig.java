package com.pardal.app.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;


@Configuration
public class FlywayConfig {

    @Bean(name = "pardalFlyway")
    public Flyway pardalFlyway(@Qualifier("pardalDataSource") DataSource pardalDataSource) {
        return Flyway.configure()
                .dataSource(pardalDataSource)
                .locations("db/migration")
                .baselineVersion("1")
                .baselineOnMigrate(true)
                .defaultSchema("pardal")
                .load();
    }

    @Bean(name = "dekFlyway")
    public Flyway dekFlyway(@Qualifier("dekDataSource") DataSource dekDataSource) {
        return Flyway.configure()
                .dataSource(dekDataSource)
                .baselineOnMigrate(true)
                .locations("db/dek_migration")
                .defaultSchema("public")
                .load();
    }


    @Bean
    public FlywayMigrationInitializer pardalFlywayInitializer(@Qualifier("pardalFlyway") Flyway pardalFlyway) {
        return new FlywayMigrationInitializer(pardalFlyway, null);
    }

    @Bean
    public FlywayMigrationInitializer dekFlywayInitializer(@Qualifier("dekFlyway") Flyway dekFlyway) {
        return new FlywayMigrationInitializer(dekFlyway, null);
    }

}
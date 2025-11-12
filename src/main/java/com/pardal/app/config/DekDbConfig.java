package com.pardal.app.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "dekEntityManagerFactory",
        transactionManagerRef = "dekTransactionManager",
        basePackages = { "com.pardal.dek.repository" }
)
public class DekDbConfig {

    @Autowired
    private Environment env;

    @Bean(name = "dekDataSource")
    @ConfigurationProperties(prefix = "dek.datasource")
    public DataSource dekDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(env.getProperty("spring.datasource.driver-class-name"))
                .url(env.getProperty("dek.datasource.url"))
                .username(env.getProperty("dek.datasource.username"))
                .password(env.getProperty("dek.datasource.password"))
                .build();
    }

    @Bean(name = "dekEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dekEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dekDataSource") DataSource dekDataSource) {

        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        hibernateProperties.put("hibernate.default_schema", "public");

        hibernateProperties.put("hibernate.connection.url", env.getProperty("dek.datasource.url"));
        hibernateProperties.put("hibernate.connection.username", env.getProperty("dek.datasource.username"));
        hibernateProperties.put("hibernate.connection.password", env.getProperty("dek.datasource.password"));

        return builder
                .dataSource(dekDataSource)
                .packages("com.pardal.dek.entity")
                .properties(hibernateProperties)
                .persistenceUnit("dek")
                .build();
    }

    @Bean(name = "dekTransactionManager")
    public PlatformTransactionManager dekTransactionManager(
            @Qualifier("dekEntityManagerFactory") EntityManagerFactory dekEntityManagerFactory) {

        return new JpaTransactionManager(dekEntityManagerFactory);
    }
}
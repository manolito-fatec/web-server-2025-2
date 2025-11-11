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
import org.springframework.context.annotation.Primary;
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
        entityManagerFactoryRef = "pardalEntityManagerFactory",
        transactionManagerRef = "pardalTransactionManager",
        basePackages = { "com.pardal.app.repository" }
)
public class PardalDbConfig {

    @Autowired
    private Environment env;

    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    @Bean(name = "pardalDataSource")
    public DataSource pardalDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(env.getProperty("spring.datasource.driver-class-name"))
                .url(env.getProperty("spring.datasource.url"))
                .username(env.getProperty("spring.datasource.username"))
                .password(env.getProperty("spring.datasource.password"))
                .build();
    }

    @Primary
    @Bean(name = "pardalEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean pardalEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("pardalDataSource") DataSource pardalDataSource) {

        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        hibernateProperties.put("hibernate.default_schema", "pardal");

        hibernateProperties.put("hibernate.connection.url", env.getProperty("spring.datasource.url"));
        hibernateProperties.put("hibernate.connection.username", env.getProperty("spring.datasource.username"));
        hibernateProperties.put("hibernate.connection.password", env.getProperty("spring.datasource.password"));


        return builder
                .dataSource(pardalDataSource)
                .packages("com.pardal.app.entity")
                .properties(hibernateProperties)
                .persistenceUnit("pardal")
                .build();
    }

    @Primary
    @Bean(name = "pardalTransactionManager")
    public PlatformTransactionManager pardalTransactionManager(
            @Qualifier("pardalEntityManagerFactory") EntityManagerFactory pardalEntityManagerFactory) {

        return new JpaTransactionManager(pardalEntityManagerFactory);
    }
}
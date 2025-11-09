package com.pardal.app.config;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@Configuration
public class MultipleMongoConfig
{
    @Primary
    @Bean(name="db1Properties")
    @ConfigurationProperties(prefix = "spring.data.mongodb.db1")
    public MongoProperties getDb1Props() throws Exception
    {
        return new MongoProperties();
    }

    @Bean(name="db2Properties")
    @ConfigurationProperties(prefix = "spring.data.mongodb.db2")
    public MongoProperties getDb2Props() throws Exception
    {
        return new MongoProperties();
    }

    @Primary
    @Bean(name = "db1MongoTamplate")
    public MongoTemplate db1MongoTemplate() throws Exception
    {
        return new MongoTemplate(db1MongoDatabaseFactory(getDb1Props()));
    }

    @Bean(name = "db2MongoTamplate")
    public MongoTemplate db2MongoTemplate() throws Exception
    {
        return new MongoTemplate(db2MongoDatabaseFactory(getDb2Props()));
    }

    @Primary
    @Bean
    public MongoDatabaseFactory db1MongoDatabaseFactory(MongoProperties mongo) throws Exception
    {
        return new SimpleMongoClientDatabaseFactory(
                mongo.getUri()
                );
    }

    @Bean
    public MongoDatabaseFactory db2MongoDatabaseFactory(MongoProperties mongo) throws Exception
    {
        return new SimpleMongoClientDatabaseFactory(
                mongo.getUri()
                );
    }

}

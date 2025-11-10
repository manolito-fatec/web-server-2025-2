package com.pardal.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"com.pardal.app.repository.logging"},
mongoTemplateRef = MongoDb2Config.MONGO_TEMPLATE
)
public class MongoDb2Config
{
        protected static final String MONGO_TEMPLATE = "db2MongoTamplate";
}

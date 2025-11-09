package com.pardal.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages =
{
        "com.pardal.app.repository.insight",
        "com.pardal.app.repository.slaPrediction",
        "com.pardal.app.repository.forecaster"
},
mongoTemplateRef = MongoDb1Config.MONGO_TEMPLATE
)
public class MongoDb1Config
{
    protected static final String MONGO_TEMPLATE = "db1MongoTamplate";
}

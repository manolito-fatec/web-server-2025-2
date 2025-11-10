package com.pardal.app.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.pardal.app.filter.MdcRequestFilter;
import com.pardal.app.repository.logging.MongoDbAppender;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;

@Configuration
public class MongoLogConfig
{
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoLogConfig(@Qualifier("db2MongoTamplate")MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshed() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        MongoDbAppender mongoAppender = new MongoDbAppender(mongoTemplate);
        mongoAppender.setContext(context);
        mongoAppender.start();

        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.addAppender(mongoAppender);
        asyncAppender.setQueueSize(5000);
        asyncAppender.setDiscardingThreshold(0);
        asyncAppender.start();

        ch.qos.logback.classic.Logger rootLogger =
                context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(asyncAppender);
    }

    @Bean
    public FilterRegistrationBean<MdcRequestFilter> mdcRequestFilterRegistration() {
        FilterRegistrationBean<MdcRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MdcRequestFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

}

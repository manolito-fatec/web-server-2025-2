package com.pardal.app.repository.logging;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.pardal.app.entity.log.LogEntry;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;


public class MongoDbAppender extends AppenderBase<ILoggingEvent>
{
    private final MongoTemplate mongoTemplate;

    public MongoDbAppender(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    protected void append(ILoggingEvent event) {
        LogEntry entry = new LogEntry();
        entry.setLevel(event.getLevel().toString());
        entry.setLogger(event.getLoggerName());
        entry.setMessage(event.getFormattedMessage());
        entry.setThread(event.getThreadName());
        entry.setTimestamp(LocalDateTime.now());
        if (event.getThrowableProxy() != null) {
            entry.setException(event.getThrowableProxy().getMessage());
        }
        Map<String, String> mdcMap = event.getMDCPropertyMap(); 

        if (mdcMap != null) {
            entry.setHttpMethod(mdcMap.get("httpMethod")); 
            entry.setRequestURI(mdcMap.get("requestURI"));
            entry.setUserEmail(mdcMap.get("userEmail"));
            entry.setRemoteIp(mdcMap.get("remoteIp"));
            entry.setTitle(mdcMap.get("title"));
        }
        mongoTemplate.save(entry);
    }
}

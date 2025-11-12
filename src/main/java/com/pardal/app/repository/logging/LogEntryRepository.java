package com.pardal.app.repository.logging;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.pardal.app.entity.log.LogEntry;

public interface LogEntryRepository extends MongoRepository<LogEntry, String>
{

    @Aggregation(pipeline = {
        "{ $match: { 'httpMethod': { $exists: true }, 'level': { $ne: 'WARN' }}}",
        "{ $limit: 5 }"
    })
    List<LogEntry> findTop5WithHttpMethod();

    @Aggregation(pipeline = {
            "{ $match: { 'httpMethod': { $exists: true }, 'level': { $ne: 'WARN' }}}"
        })
    List<LogEntry> findAllAuditLogs();
}

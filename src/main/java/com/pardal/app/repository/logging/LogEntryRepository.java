package com.pardal.app.repository.logging;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pardal.app.entity.log.LogEntry;

public interface LogEntryRepository extends MongoRepository<LogEntry, String>
{

}

package com.pardal.app.repository.insight;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pardal.app.entity.documents.TicketInsight;

public interface InsightRepository extends MongoRepository<TicketInsight, String>, InsightRepositoryCustom
{

}

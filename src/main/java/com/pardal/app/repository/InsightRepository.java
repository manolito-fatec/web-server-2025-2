package com.pardal.app.repository;

import com.pardal.app.entity.documents.TicketInsight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Aggregation;

import java.util.List;

public interface InsightRepository extends MongoRepository<TicketInsight, String> {

    List<TicketInsight> findByCompanyId(Integer companyId);

    /**
     * Finds the most recent insight (based on the 'dth' field) for each product
     * of a specific company.
     *
     * @param companyId The company's ID.
     * @return A list containing the latest TicketInsight for each product.
     */
    @Aggregation(pipeline = {
            "{ $match: { company_id: ?0 } }",
            "{ $sort: { dth: -1 } }",
            "{ $group: { " +
                    "_id: '$product_id'," +
                    "latest_doc: { $first: '$$ROOT' }" +
                    "} }",
            "{ $replaceRoot: { newRoot: '$latest_doc' } }",
            "{ $sort: { 'product_id': 1 } }"
    })
    List<TicketInsight> findLatestInsightsByCompanyId(Integer companyId);

}
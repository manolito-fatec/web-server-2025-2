package com.pardal.app.repository.insight;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.pardal.app.entity.documents.TicketInsight;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InsightRepositoryCustomImpl implements InsightRepositoryCustom
{
    private final MongoTemplate mongoTemplate;
    private final Class<TicketInsight> INSIGHT_COLLECTION = TicketInsight.class;

    @Override
    public List<TicketInsight> findLatestInsightsByCompanyIdOurProductId (List<Integer> customersIdList, List<Integer> productIdList)
    {
        List<AggregationOperation> operations = new ArrayList<>();

        if (customersIdList != null && !customersIdList.isEmpty()) {
            operations.add(Aggregation.match(
                    Criteria.where("company_id").in(customersIdList)
                    ));
        }

        if (productIdList != null && !productIdList.isEmpty()) {
            operations.add(Aggregation.match(
                    Criteria.where("product_id").in(productIdList)
                    ));
        }

        operations.add(Aggregation.sort(Sort.Direction.DESC, "dth"));

        operations.add(Aggregation.group("company_id", "product_id") 
                .first("$$ROOT").as("latest_doc"));

        operations.add(Aggregation.replaceRoot("latest_doc"));

        operations.add(Aggregation.sort(Sort.Direction.ASC, "company_id", "product_id"));

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<TicketInsight> results = mongoTemplate.aggregate(
                aggregation, 
                mongoTemplate.getCollectionName(INSIGHT_COLLECTION),
                INSIGHT_COLLECTION
                );

        return results.getMappedResults();
    }

}

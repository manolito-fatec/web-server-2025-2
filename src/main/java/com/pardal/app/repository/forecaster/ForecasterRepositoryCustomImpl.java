package com.pardal.app.repository.forecaster;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.pardal.app.entity.documents.Forecaster;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ForecasterRepositoryCustomImpl implements ForecasterRepositoryCustom
{
    private final MongoTemplate mongoTemplate;
    private final Class<Forecaster> FORECASTER_COLLECTION = Forecaster.class;

    @Override
    public List<Forecaster> findByCompanyIdOrProductId ( List<Integer> companyIdList, List<Integer> productIdList )
    {
        List<AggregationOperation> operations = new ArrayList<>();

        Criteria criteria = addCriteria(companyIdList, productIdList);

        if (criteria != null)
        {
            operations.add(Aggregation.match(criteria));
        }

        operations.add(Aggregation.sort(Sort.Direction.DESC, "dth"));

        Aggregation aggregation = Aggregation.newAggregation(operations);

        AggregationResults<Forecaster> results = mongoTemplate.aggregate(
                aggregation,
                mongoTemplate.getCollectionName(FORECASTER_COLLECTION),
                FORECASTER_COLLECTION
                );

        return results.getMappedResults();
    }

    private String getLastVersionDataBase()
    {
        Aggregation aggregation = Aggregation.newAggregation(

                Aggregation.sort(Sort.Direction.DESC, "dth"),
                Aggregation.limit(1)
                );
        AggregationResults<Forecaster> results = mongoTemplate.aggregate(
                aggregation,
                mongoTemplate.getCollectionName(FORECASTER_COLLECTION),
               FORECASTER_COLLECTION
                );

        return results.getMappedResults().getFirst().getDth();
    }

    private Criteria addCriteria(List<Integer> companyIdList, List<Integer> productIdList)
    {
        Criteria criteria = new Criteria();
        String dth = getLastVersionDataBase();

        if(dth == null)
        {
            return null;
        }

        criteria.and("dth").is(dth);

        if (companyIdList != null && !companyIdList.isEmpty())
        {
            criteria.and("companyId").in(companyIdList);
        }

        if (productIdList != null && !productIdList.isEmpty())
        {
            criteria.and("productId").in(productIdList);
        }
        return criteria;
    }
}

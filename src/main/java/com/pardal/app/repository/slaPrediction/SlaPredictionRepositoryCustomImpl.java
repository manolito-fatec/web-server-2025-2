package com.pardal.app.repository.slaPrediction;

import com.pardal.app.entity.dto.insights.SlaPredictionResponseDto;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class SlaPredictionRepositoryCustomImpl implements SlaPredictionRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public SlaPredictionRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<SlaPredictionResponseDto> findTop3ByCompanyIdGroupedBySubcategory(List<Integer> companyIdList, List<Integer> productIdList) {
        Criteria criteria = new Criteria();

        if (companyIdList != null && !companyIdList.isEmpty()) {
            criteria.and("company_id").in(companyIdList);
        }
 
        if (productIdList != null && !productIdList.isEmpty()) {
            criteria.and("product_id").in(productIdList);
        }

        MatchOperation matchStage = match(criteria);

        GroupOperation groupStage = group("subcategory_id", "subcategory_name")
                .avg("sla_breach_probability").as("averageRiskProbability");

        SortOperation sortStage = sort(DESC, "averageRiskProbability");

        ProjectionOperation projectStage = project()
                .andExclude("_id")
                .and("_id.subcategory_id").as("subcategoryId")
                .and("_id.subcategory_name").as("subcategoryName")
                .andInclude("averageRiskProbability");

        Aggregation aggregation = newAggregation(
                matchStage,
                groupStage,
                sortStage,
                limit(3),
                projectStage
        );

        AggregationResults<SlaPredictionResponseDto> results = mongoTemplate.aggregate(
                aggregation,
                "sla_predictions",
                SlaPredictionResponseDto.class
        );

        return results.getMappedResults();
    }
}
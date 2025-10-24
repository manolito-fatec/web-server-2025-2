package com.pardal.app.repository;

import com.pardal.app.entity.documents.SlaPrediction;
import com.pardal.app.entity.dto.insights.SlaPredictionResponseDto;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SlaPredictionRepository extends MongoRepository<SlaPrediction, String> {

    /**
     * Finds the most recent prediction date (dth) for a specific company,
     * then aggregates subcategories, calculates the average SLA breach probability,
     * and returns the top 3 subcategories with the highest risk.
     *
     * @param companyId The ID of the company.
     * @return A list of the top 3 subcategories by risk for the specified company.
     */
    @Aggregation(pipeline = {
            "{ $match: { companyId: ?0 } }",
            "{ $group: { _id: '$companyId', maxDth: { $max: '$dth' } } }",
            "{ $lookup: { " +
                    "from: 'sla_predictions', " +
                    "let: { targetCompanyId: '$_id', latestDth: '$maxDth' }, " +
                    "pipeline: [" +
                    "{ $match: { $expr: { $and: [ " +
                    "{ $eq: ['$companyId', '$$targetCompanyId'] }, " +
                    "{ $eq: ['$dth', '$$latestDth'] } " +
                    "] } } }" +
                    "], " +
                    "as: 'latestCompanyDocs' " +
                    "} }",
            "{ $unwind: '$latestCompanyDocs' }",
            "{ $replaceRoot: { newRoot: '$latestCompanyDocs' } }",
            "{ $group: { " +
                    "_id: '$subcategoryId', " +
                    "subcategoryName: { $first: '$subcategoryName' }, " +
                    "averageRiskProbability: { $avg: '$slaBreachProbability' } " +
                    "} }",
            "{ $project: { " +
                    "_id: 0, " +
                    "subcategoryId: '$_id'," +
                    "subcategoryName: 1," +
                    "averageRiskProbability: 1 " +
                    "} }",
            "{ $sort: { averageRiskProbability: -1 } }",
            "{ $limit: 3 }"
    })
    List<SlaPredictionResponseDto> findTop3RiskSubcategoriesByCompany(Integer companyId);


    /**
     * Finds the most recent prediction date (dth) GLOBALLY across all predictions,
     * then aggregates subcategories (for ALL companies combined on that date),
     * calculates the average SLA breach probability, and returns the top 3
     * subcategories with the highest risk.
     *
     * @return A list of the top 3 subcategories by risk considering all companies.
     */
    @Aggregation(pipeline = {
            "{ $group: { _id: null, maxDth: { $max: '$dth' } } }",
            "{ $lookup: { " +
                    "from: 'sla_predictions', " +
                    "let: { latestDth: '$maxDth' }, " +
                    "pipeline: [" +
                    "{ $match: { $expr: { $eq: ['$dth', '$$latestDth'] } } }" +
                    "], " +
                    "as: 'latestGlobalDocs' " +
                    "} }",
            "{ $unwind: '$latestGlobalDocs' }",
            "{ $replaceRoot: { newRoot: '$latestGlobalDocs' } }",
            "{ $group: { " +
                    "_id: '$subcategoryId', " +
                    "subcategoryName: { $first: '$subcategoryName' }, " +
                    "averageRiskProbability: { $avg: '$slaBreachProbability' } " +
                    "} }",
            "{ $project: { " +
                    "_id: 0, " +
                    "subcategoryId: '$_id'," +
                    "subcategoryName: 1," +
                    "averageRiskProbability: 1 " +
                    "} }",
            "{ $sort: { averageRiskProbability: -1 } }",
            "{ $limit: 3 }"
    })
    List<SlaPredictionResponseDto> findTop3RiskSubcategoriesForAllCompanies();

}
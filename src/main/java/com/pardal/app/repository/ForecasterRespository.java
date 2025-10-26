package com.pardal.app.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.pardal.app.entity.documents.Forecaster;

public interface ForecasterRespository extends MongoRepository<Forecaster, String>{

    @Aggregation(pipeline = {
            "{ $match: { companyId: ?0 } }",

            "{ $group: { _id: null, maxDth: { $max: '$dth' } } }",
            "{ $lookup: { " +
                "from: 'tickets_forecaster', " +
                "let: { maxDth: '$maxDth' }, " +
                "pipeline: [" +
                    "{ $match: { $expr: { $and: [ " +
                        "{ $eq: ['$companyId', ?0] }, " +
                        "{ $eq: ['$dth', '$$maxDth'] } " +
                    "] } } }" +
                "], " +
                "as: 'latestDocs' " +
            "} }",
            "{ $unwind: '$latestDocs' }",
            "{ $replaceRoot: { newRoot: '$latestDocs' } }"
        })
    List<Forecaster> findByCompanyId(Integer companyId);

    @Aggregation(pipeline = {
            "{ $group: { _id: null, maxDth: { $max: '$dth' } } }",
            "{ $lookup: { " +
                "from: 'tickets_forecaster', " +
                "let: { maxDth: '$maxDth' }, " +
                "pipeline: [" +
                    "{ $match: { $expr: { $eq: ['$dth', '$$maxDth'] } } }" +
                "], " +
                "as: 'latestDocs' " +
            "} }",
            "{ $unwind: '$latestDocs' }",
            "{ $replaceRoot: { newRoot: '$latestDocs' } }"
        })
    List<Forecaster> findAllCompanies();

}

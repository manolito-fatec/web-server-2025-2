package com.pardal.app.repository.slaPrediction;

import com.pardal.app.entity.documents.SlaPrediction;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SlaPredictionRepository extends MongoRepository<SlaPrediction, String>, SlaPredictionRepositoryCustom {

}
package com.pardal.app.repository.forecaster;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pardal.app.entity.documents.Forecaster;

public interface ForecasterRespository extends MongoRepository<Forecaster, String>, ForecasterRepositoryCustom {
}

package com.pardal.app.repository.forecaster;

import java.util.List;

import com.pardal.app.entity.documents.Forecaster;

public interface ForecasterRepositoryCustom
{
    List<Forecaster> findByCompanyIdOrProductId(List<Integer> companyIdList, List<Integer> productIdList);
}

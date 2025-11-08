package com.pardal.app.repository.forecaster;

import java.util.List;

import com.pardal.app.entity.documents.Forecaster;

public interface ForecasterRepositoryCustom
{
    List<Forecaster> findByCompanyIdOurProductId(List<Integer> companyIdList, List<Integer> productIdList);
}

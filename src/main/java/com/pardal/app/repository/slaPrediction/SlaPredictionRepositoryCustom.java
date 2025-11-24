package com.pardal.app.repository.slaPrediction;

import com.pardal.app.entity.dto.insights.SlaPredictionResponseDto;

import java.util.List;

public interface SlaPredictionRepositoryCustom {
    List<SlaPredictionResponseDto> findTop3ByCompanyIdGroupedBySubcategory(List<Integer> companyIdList, List<Integer> productIdList);
}
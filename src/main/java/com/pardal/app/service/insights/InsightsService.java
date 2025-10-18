package com.pardal.app.service.insights;

import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;

public interface InsightsService {
    InsightsDataDto getAllInsightsData(InsightsFilterDto filters);
}
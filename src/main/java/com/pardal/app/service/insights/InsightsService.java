package com.pardal.app.service.insights;

import com.pardal.app.entity.documents.TicketInsight;
import com.pardal.app.entity.dto.insights.InsightsDataDto;
import com.pardal.app.entity.dto.insights.InsightsFilterDto;

import java.util.List;

public interface InsightsService {
    InsightsDataDto getAllInsightsData(InsightsFilterDto filters);

    List<TicketInsight> findByCompanyId(Integer companyId);
}
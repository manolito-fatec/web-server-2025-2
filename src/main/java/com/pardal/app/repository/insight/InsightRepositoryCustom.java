package com.pardal.app.repository.insight;

import java.util.List;

import com.pardal.app.entity.documents.TicketInsight;

public interface InsightRepositoryCustom
{
    List<TicketInsight> findLatestInsightsByCompanyIdOrProductId(List<Integer> customerIdList, List<Integer> productIdList );
}

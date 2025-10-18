package com.pardal.app.service.insight;

import com.pardal.app.entity.TicketInsight;

import java.util.List;

public interface InsightService {


    List<TicketInsight> findByCompanyId(Integer companyId);

    List<TicketInsight> findLatestByCompanyId(Integer companyId);
}

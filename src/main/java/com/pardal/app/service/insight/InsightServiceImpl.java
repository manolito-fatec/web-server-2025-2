package com.pardal.app.service.insight;

import com.pardal.app.entity.TicketInsight;
import com.pardal.app.repository.InsightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

import java.util.List;

@Service
public class InsightServiceImpl implements InsightService {

    @Autowired
    private InsightRepository insightRepository;


    @Override
    public List<TicketInsight> findByCompanyId(Integer companyId) {
        return insightRepository.findByCompanyId(companyId);
    }

    @Override
    public List<TicketInsight> findLatestByCompanyId(Integer companyId) {
        List<TicketInsight> insights = insightRepository.findLatestInsightsByCompanyId(companyId);

        if (insights.isEmpty()) {
            throw new NoSuchElementException("No insights found for company ID: " + companyId);
        }

        return insights;
    }
}

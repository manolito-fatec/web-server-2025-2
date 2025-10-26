package com.pardal.app.entity.dto.insights;

import com.pardal.app.entity.documents.Forecaster;
import com.pardal.app.entity.documents.TicketInsight;
import com.pardal.app.entity.dto.metrics.TicketsBySubcategoryCountDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InsightsDataDto {
    // SLA
    private List<SlaPredictionResponseDto> slaInsightData;

    // sazonalidade
    private List<Forecaster> seasonalityInsightData;

    // insights de produto
    private List<TicketInsight> productInsightsData;

    private List<TicketsBySubcategoryCountDto> paretoInsightData;
}

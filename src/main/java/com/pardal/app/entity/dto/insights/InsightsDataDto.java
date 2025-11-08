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

    private List<SlaPredictionResponseDto> slaInsightData;

    private List<Forecaster> seasonalityInsightData;

    private List<TicketInsight> productInsightsData;

    private List<TicketsBySubcategoryCountDto> paretoInsightData;
}

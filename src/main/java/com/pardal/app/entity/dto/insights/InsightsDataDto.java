package com.pardal.app.entity.dto.insights;

import com.pardal.app.entity.documents.Forecaster;
import com.pardal.app.entity.documents.TicketInsight;
import com.pardal.app.entity.dto.metrics.TicketsBySubcategoryCountDto;
import com.pardal.app.util.Gambiarra;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Gambiarra(descricao = "varios objetos genericos enquanto os services nao gerarem os objetos para retorno",
        autor = "AndreWakugawa",
        data = "2025/10/16"
)
public class InsightsDataDto {
    // SLA
    private Object slaInsightData;

    // sazonalidade
    private List<Forecaster> seasonalityInsightData;

    // insights de produto
    private List<TicketInsight> productInsightsData;

    private List<TicketsBySubcategoryCountDto> paretoInsightData;
}

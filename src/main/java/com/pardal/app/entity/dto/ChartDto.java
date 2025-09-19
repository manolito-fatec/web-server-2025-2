package com.pardal.app.entity.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChartDto
{
    private BigDecimal recidivismRate;
    private List<TicketsByProductsCountDto> ticketsCountGroupedByProduct;
    private double slaCompliancePercentualDto;
}

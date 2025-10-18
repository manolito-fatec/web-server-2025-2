package com.pardal.app.entity.dto.insights;

import com.pardal.app.repository.specification.tickets.TicketsFilters;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class InsightsFilterDto extends TicketsFilters {
    private Long clientId; // se null = todos os clientes
    // add aqui outros filtros se precisar ou tiver faltando algum
}
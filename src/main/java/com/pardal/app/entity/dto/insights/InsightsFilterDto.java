package com.pardal.app.entity.dto.insights;

import com.pardal.app.repository.specification.tickets.TicketsFilters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InsightsFilterDto extends TicketsFilters {
    private Integer customerId; // se null = todos os clientes
    // add aqui outros filtros se precisar ou tiver faltando algum
}
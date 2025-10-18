package com.pardal.app.entity.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TicketCountDto  {
    private String period;
    private Long totalTickets;
}

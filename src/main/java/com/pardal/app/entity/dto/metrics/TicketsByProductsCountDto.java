package com.pardal.app.entity.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TicketsByProductsCountDto{
    private Integer productId;
    private String productName;
    private Long totalTickets;
}

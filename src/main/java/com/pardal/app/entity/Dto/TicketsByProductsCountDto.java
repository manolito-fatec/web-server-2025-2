package com.pardal.app.entity.Dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class TicketsByProductsCountDto {
    private Integer productId;
    private Long totalTickets;
}

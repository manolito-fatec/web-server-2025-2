package com.pardal.app.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TicketsByProductsCountDto{
    private Integer productId;
    private Long totalTickets;
}

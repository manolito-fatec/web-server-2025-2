package com.pardal.app.entity.Dto;

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

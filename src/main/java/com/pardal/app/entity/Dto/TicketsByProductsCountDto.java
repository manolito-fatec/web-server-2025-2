package com.pardal.app.entity.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketsByProductsCountDto {
    private Integer productId;
    private Long totalTickets;

}

package com.pardal.app.entity.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketsBySubcategoryCountDto {
    private Integer subcategoryId;
    private String subcategoryName;
    private Integer companyId;
    private String companyName;
    private Long totalTickets;
}

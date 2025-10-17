package com.pardal.app.repository.specification.tickets;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketsFilters {
    private Integer productId;
    private Integer customerId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}

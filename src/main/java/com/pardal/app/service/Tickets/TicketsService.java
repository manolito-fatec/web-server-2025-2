package com.pardal.app.service.Tickets;

import com.pardal.app.entity.Tickets;

import java.util.List;

public interface TicketsService {
    List<Tickets> getTicketsByProductId(Integer productId);
}

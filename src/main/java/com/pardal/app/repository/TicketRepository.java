package com.pardal.app.repository;

import com.pardal.app.entity.Tickets;
import org.springframework.data.repository.Repository;

public interface TicketRepository extends Repository<Tickets, Long> {
}
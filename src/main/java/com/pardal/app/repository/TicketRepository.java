package com.pardal.app.repository;

import com.pardal.app.entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<Tickets, Long>, JpaSpecificationExecutor<Tickets> {


}
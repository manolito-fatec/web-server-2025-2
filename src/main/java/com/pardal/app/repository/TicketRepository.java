package com.pardal.app.repository;

import com.pardal.app.entity.dto.metrics.TicketsByProductsCountDto;
import com.pardal.app.entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Tickets, Long>, JpaSpecificationExecutor<Tickets> {
    @Query("SELECT new com.pardal.app.entity.dto.metrics.TicketsByProductsCountDto(t.product.id, t.product.name, COUNT(t)) FROM Tickets t GROUP BY t.product.id, t.product.name")
    List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct();
}

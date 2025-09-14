package com.pardal.app.repository;

import com.pardal.app.entity.Dto.TicketsByProductsCountDto;
import com.pardal.app.entity.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, Long>, JpaSpecificationExecutor<Tickets> {
    @Query("SELECT new com.pardal.app.entity.Dto.TicketsByProductsCountDto(t.product.id, COUNT(t)) FROM Tickets t GROUP BY t.product.id")
    List<TicketsByProductsCountDto> getTicketsCountGroupedByProduct();
}
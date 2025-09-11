package com.pardal.app.repository;

import com.pardal.app.entity.Tickets;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Tickets, Long> {

    @Query("SELECT t FROM Tickets t WHERE t.productId = :productId")
    List<Tickets> findByProductId(@Param("productId") Integer productId);
}
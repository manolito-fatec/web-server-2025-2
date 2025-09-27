package com.pardal.app.repository;

import com.pardal.app.entity.TicketStatusHistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, Long>, JpaSpecificationExecutor<TicketStatusHistory>
{

}
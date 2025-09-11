package com.pardal.app.service.Tickets;

import com.pardal.app.entity.Tickets;
import com.pardal.app.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketsServiceImpl implements TicketsService {

    @Autowired
    private TicketRepository ticketsRepository;

    @Override
    public List<Tickets> getTicketsByProductId(Integer productId) {
        return ticketsRepository.findByProductId(productId);
    }
}